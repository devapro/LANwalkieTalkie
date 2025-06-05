package pro.devapp.walkietalkiek.serivce.network

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import timber.log.Timber

class RegistrationListener(private val chanelController: ChanelController) :
    NsdManager.RegistrationListener {
    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Timber.Forest.e("onRegistrationFailed: $serviceInfo ($errorCode)")
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        Timber.Forest.i("onServiceUnregistered: $serviceInfo")
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Timber.Forest.i("onUnregistrationFailed: $serviceInfo ($errorCode)")
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Timber.Forest.i("onServiceRegistered: $serviceInfo")
        chanelController.onServiceRegister()
    }
}