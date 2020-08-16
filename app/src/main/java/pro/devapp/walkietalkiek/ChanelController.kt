package pro.devapp.walkietalkiek

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.DiscoveryListener
import pro.devapp.walkietalkiek.service.RegistrationListener
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.Executors


class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()

    private val client = Client()
    private val server = Server()

    private val resolver = Resolver(nsdManager) { addr, nsdServiceInfo ->
        client.addClient(addr, nsdServiceInfo)
    }

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */ /* WiFi Walkie Talkie */
    }

    fun startDiscovery() {
        nsdManager.apply {
            val port = server.initServer()
            registerService(port)
        }
    }

    fun stopDiscovery() {
        nsdManager.apply {
            stopServiceDiscovery(discoveryListener)
            unregisterService(registrationListener)
        }
        executor.shutdown()
        client.stop()
    }

    fun onServiceRegister() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        server.sendMessage(byteBuffer)
    }

    private fun registerService(port: Int) {
        Timber.i("registerService")
        val result = deviceInfoRepository.getCurrentDeviceInfo()
        result.getOrNull()?.apply {
            /* Android NSD implementation is very unstable when services
                    * registers with the same name. Will use "CHANNEL_NAME:DEVICE_ID:".
                    */
            val serviceInfo = NsdServiceInfo()
            val encodedName = Base64.encodeToString(
                name.toByteArray(),
                Base64.NO_PADDING or Base64.NO_WRAP
            )
            val serviceName = "$encodedName:$deviceId:"
            serviceInfo.serviceType = SERVICE_TYPE
            serviceInfo.serviceName = serviceName
            currentServiceName = serviceName
            serviceInfo.port = port
            Timber.i("try register $name: $serviceInfo")
            nsdManager.registerService(
                serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener
            )
        }
        result.exceptionOrNull()?.apply {
            Timber.w(this)
        }
    }


    fun onServiceFound(serviceInfo: NsdServiceInfo) {
        Timber.i("onServiceFound: $serviceInfo")
        // check for self add to list
        if (serviceInfo.serviceName == currentServiceName) {
            Timber.i("onServiceFound: SELF")
            return
        }
        resolver.resolve(serviceInfo)
    }

    fun onServiceLost(serviceInfo: NsdServiceInfo) {
        Timber.i("onServiceLost: $serviceInfo")
        if (serviceInfo.serviceName == currentServiceName) {
            Timber.i("onServiceLost: SELF")
            return
        }
        client.removeClient(serviceInfo)
    }
}