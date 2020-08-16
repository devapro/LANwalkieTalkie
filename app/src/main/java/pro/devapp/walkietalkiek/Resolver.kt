package pro.devapp.walkietalkiek

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import timber.log.Timber
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

class Resolver(
    private val nsdManager: NsdManager,
    private val resultListener: (addr: InetSocketAddress, nsdServiceInfo: NsdServiceInfo) -> Unit
) {
    private val servicesResolverQueue = LinkedBlockingDeque<NsdServiceInfo>()
    private val isResolverInProgress = AtomicBoolean(false)

    fun resolve(nsdServiceInfo: NsdServiceInfo) {
        Timber.i("resolve")
        servicesResolverQueue.add(nsdServiceInfo)
        resolveNext()
    }

    private fun resolveNext() {
        Timber.i("resolveNext")
        if (isResolverInProgress.compareAndSet(false, true)) {
            Timber.i("resolveNext start")
            if (servicesResolverQueue.isNotEmpty()) {
                val nsdInfo = servicesResolverQueue.pollFirst()
                Timber.i("resolveNext $nsdInfo")
                nsdManager.resolveService(nsdInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        isResolverInProgress.set(false)
                        Timber.i("onResolveFailed")
                        resolveNext()
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val addr = InetSocketAddress(serviceInfo.host, serviceInfo.port)
                        Timber.i("onServiceResolved: $addr")
                        if (!addr.address.isMulticastAddress) {
                            resultListener(addr, serviceInfo)
                        }
                        isResolverInProgress.set(false)
                        resolveNext()
                    }
                })
            } else {
                isResolverInProgress.set(false)
            }
        } else {
            Timber.i("resolveNext in progress")
        }
    }
}