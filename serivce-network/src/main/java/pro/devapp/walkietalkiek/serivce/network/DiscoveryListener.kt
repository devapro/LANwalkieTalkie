package pro.devapp.walkietalkiek.serivce.network

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresExtension
import timber.log.Timber

internal class DiscoveryListener(
    private val chanelController: ChanelControllerImpl
) : NsdManager.DiscoveryListener {
    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        try {
            val ss = serviceInfo.serviceName.split(":").toTypedArray()
            val channelName = String(Base64.decode(ss[0], 0))
            Timber.Forest.i("DiscoveryListener: $channelName: $serviceInfo")
            chanelController.onServiceFound(serviceInfo)
        } catch (e: IllegalArgumentException) {
            Timber.Forest.w(e)
        }
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Timber.Forest.i("Stop discovery failed: $errorCode")
    }

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Timber.Forest.i("Start discovery failed: $errorCode")
    }

    override fun onDiscoveryStarted(serviceType: String?) {
        Timber.Forest.i("Discovery started")
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        Timber.Forest.i("Discovery stopped")
    }

    @RequiresExtension(extension = Build.VERSION_CODES.TIRAMISU, version = 7)
    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        try {
            val ss =
                serviceInfo.serviceName.split(":")
                    .toTypedArray()
            val channelName = String(Base64.decode(ss[0], 0))
            Timber.Forest.i("onServiceLost: $channelName: $serviceInfo")
            chanelController.onServiceLost(serviceInfo)
        } catch (e: IllegalArgumentException) {
            Timber.Forest.w(e)
        }
    }
}