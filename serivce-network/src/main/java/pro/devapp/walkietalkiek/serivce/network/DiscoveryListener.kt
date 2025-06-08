package pro.devapp.walkietalkiek.serivce.network

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import timber.log.Timber

class DiscoveryListener(
    private val chanelController: ChanelController
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