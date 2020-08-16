package pro.devapp.walkietalkiek

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import timber.log.Timber
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

class Resolver(
    private val nsdManager: NsdManager,
    private val resultListener: (addr: InetSocketAddress, nsdServiceInfo: NsdServiceInfo) -> Unit
) {
    private val servicesResolverQueue = LinkedBlockingDeque<NsdServiceInfo>()
    private val isResolverInProgress = AtomicInteger(0)

    fun resolve(nsdServiceInfo: NsdServiceInfo) {
        Timber.i("resolve")
        servicesResolverQueue.add(nsdServiceInfo)
        resolveNext()
    }

    private fun resolveNext() {
        Timber.i("resolveNext")
        if (isResolverInProgress.compareAndSet(0, 1)) {
            Timber.i("resolveNext start")
            if (servicesResolverQueue.isNotEmpty()) {
                val nsdInfo = servicesResolverQueue.pollFirst()
                Timber.i("resolveNext $nsdInfo")
                nsdManager.resolveService(nsdInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        isResolverInProgress.set(0)
                        Timber.i("onResolveFailed")
                        resolveNext()
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        isResolverInProgress.set(0)
                        val addr = InetSocketAddress(serviceInfo.host, serviceInfo.port)
                        Timber.i("onServiceResolved: $addr")
                        if (!addr.address.isMulticastAddress) {
                            resultListener(addr, serviceInfo)
                        }
                        resolveNext()
                    }
                })
            } else {
                isResolverInProgress.set(0)
            }
        } else {
            val count = isResolverInProgress.getAndAdd(1)
            Timber.w("resolveNext in progress $count")
            if (count > 3) {
                isResolverInProgress.set(0)
            }
        }
    }
}