package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import android.util.Log
import pro.devapp.walkietalkiek.ChanelController

class DiscoveryListener(private val chanelController: ChanelController) :
    NsdManager.DiscoveryListener {
    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        try {
            val ss = serviceInfo.serviceName.split(":").toTypedArray()
            val channelName = String(Base64.decode(ss[0], 0))
            Log.i(
                "DiscoveryListener",
                "onServiceFound: $channelName: $serviceInfo"
            )
            chanelController.resolveService(serviceInfo)
        } catch (ex: IllegalArgumentException) {
            Log.w("DiscoveryListener", ex.toString())
        }
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Log.e("DiscoveryListener", "Stop discovery failed: $errorCode")
    }

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
        Log.e("DiscoveryListener", "Start discovery failed: $errorCode")
    }

    override fun onDiscoveryStarted(serviceType: String?) {
        Log.i("DiscoveryListener", "Discovery started")
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        Log.i("DiscoveryListener", "Discovery stopped")
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        try {
            val ss =
                serviceInfo.serviceName.split(":")
                    .toTypedArray()
            val channelName = String(Base64.decode(ss[0], 0))
            Log.i(
                "DiscoveryListener",
                "onServiceLost: $channelName: $serviceInfo"
            )
        } catch (ex: IllegalArgumentException) {
            Log.w("DiscoveryListener", ex.toString())
        }
    }
}