package pro.devapp.walkietalkiek.serivce.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.annotation.RequiresExtension
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import timber.log.Timber
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class ClientInfoResolver(
    private val context: Context,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val scope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val executor = ThreadPoolExecutor(
        0, 1, 60L, TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>()
    )

    @RequiresExtension(
        extension = Build.VERSION_CODES.TIRAMISU,
        version = 7
    )
    fun resolve(
        nsdInfo: NsdServiceInfo,
        resultListener: (socketAddress: InetSocketAddress, nsdServiceInfo: NsdServiceInfo) -> Unit
        ) {
        Timber.i("resolve")
        scope.launch {
            Timber.i("resolveNext $nsdInfo")
            nsdManager.registerServiceInfoCallback(nsdInfo, executor,
            object : NsdManager.ServiceInfoCallback {
                override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                    Timber.i("onResolveFailed")
                }

                override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                    val socketAddress = InetSocketAddress(serviceInfo.host, serviceInfo.port)
                    Timber.i("onServiceResolved: $socketAddress")
                    if (!socketAddress.address.isMulticastAddress) {
                        resultListener(socketAddress, serviceInfo)
                    }
                }

                override fun onServiceLost() {
                    Timber.i("onServiceLost")
                }

                override fun onServiceInfoCallbackUnregistered() {

                }
            })
//            nsdManager.resolveService(nsdInfo, object : NsdManager.ResolveListener {
//                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
//                    Timber.i("onResolveFailed")
//                }
//
//                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
//                    val socketAddress = InetSocketAddress(serviceInfo.host, serviceInfo.port)
//                    Timber.i("onServiceResolved: $socketAddress")
//                    if (!socketAddress.address.isMulticastAddress) {
//                        resultListener(socketAddress, serviceInfo)
//                    }
//                }
//            })
        }
    }
}