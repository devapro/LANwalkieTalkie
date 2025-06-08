package pro.devapp.walkietalkiek.serivce.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import timber.log.Timber
import java.net.InetSocketAddress

class ClientInfoResolver(
    private val context: Context,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val scope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    fun resolve(
        nsdInfo: NsdServiceInfo,
        resultListener: (socketAddress: InetSocketAddress, nsdServiceInfo: NsdServiceInfo) -> Unit
        ) {
        Timber.i("resolve")
        scope.launch {
            Timber.i("resolveNext $nsdInfo")
            nsdManager.resolveService(nsdInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Timber.i("onResolveFailed")
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val socketAddress = InetSocketAddress(serviceInfo.host, serviceInfo.port)
                    Timber.i("onServiceResolved: $socketAddress")
                    if (!socketAddress.address.isMulticastAddress) {
                        resultListener(socketAddress, serviceInfo)
                    }
                }
            })
        }
    }
}