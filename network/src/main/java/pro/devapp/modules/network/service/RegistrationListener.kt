package pro.devapp.modules.network.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import timber.log.Timber

class RegistrationListener(private val chanelController: ChanelController) :
    NsdManager.RegistrationListener {
    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Timber.e("onRegistrationFailed: $serviceInfo ($errorCode)")
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        Timber.i("onServiceUnregistered: $serviceInfo")
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Timber.i("onUnregistrationFailed: $serviceInfo ($errorCode)")
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Timber.i("onServiceRegistered: $serviceInfo")
        chanelController.onServiceRegister()
    }
}