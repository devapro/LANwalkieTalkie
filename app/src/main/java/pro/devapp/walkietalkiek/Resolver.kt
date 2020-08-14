package pro.devapp.walkietalkiek

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

class Resolver(
    private val nsdManager: NsdManager,
    private val resultListener: (addr: InetSocketAddress) -> Unit
) {
    private val servicesResolverQueue = LinkedBlockingDeque<NsdServiceInfo>()
    private val isResolverInProgress = AtomicBoolean(false)

    companion object {
        const val LOG_TAG = "Resolver"
    }

    fun resolve(nsdServiceInfo: NsdServiceInfo) {
        servicesResolverQueue.add(nsdServiceInfo)
        resolveNext()
    }

    private fun resolveNext() {
        if (isResolverInProgress.compareAndSet(false, true)) {
            if (servicesResolverQueue.isNotEmpty()) {
                val nsdInfo = servicesResolverQueue.pollFirst()
                nsdManager.resolveService(nsdInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        isResolverInProgress.set(false)
                        Log.d(LOG_TAG, "onResolveFailed")
                        resolveNext()
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        isResolverInProgress.set(false)
                        val addr = InetSocketAddress(serviceInfo.host, serviceInfo.port)
                        Log.d(LOG_TAG, "onServiceResolved: $addr")
                        if (!addr.address.isMulticastAddress) {
                            resultListener(addr)
                        }
                        resolveNext()
                    }
                })
            } else {
                isResolverInProgress.set(false)
            }
        }
    }
}