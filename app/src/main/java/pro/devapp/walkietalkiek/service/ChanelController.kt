package pro.devapp.walkietalkiek.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import pro.devapp.walkietalkiek.VoicePlayer
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)
    private val resolvedNamesCache = HashMap<String, String>()

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()

    private val voicePlayer = VoicePlayer()
    private val client = SocketClient() {
        voicePlayer.play(it)
    }
    private val server = SocketServer(object : IServer.ConnectionListener {
        override fun onClientConnected(address: InetSocketAddress) {
            // try connect to new client
            client.addClient(address, false)
        }

        override fun onClientDisconnected(address: InetSocketAddress) {
            client.removeClient(address.address.hostAddress)
        }
    })

    private val resolver =
        Resolver(nsdManager) { addr, nsdServiceInfo ->
            Timber.i("Resolve: ${nsdServiceInfo.serviceName}")
            resolvedNamesCache[nsdServiceInfo.serviceName] = addr.address.hostAddress
            client.addClient(addr)
        }

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */
    }

    fun startDiscovery() {
        nsdManager.apply {
            val port = server.initServer()
            registerService(port)
        }
        voicePlayer.create()
        voicePlayer.startPlay()
    }

    fun stopDiscovery() {
        nsdManager.apply {
            stopServiceDiscovery(discoveryListener)
            unregisterService(registrationListener)
        }
        executor.shutdown()
        client.stop()
        server.stop()
        voicePlayer.stopPlay()
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
            serviceInfo.serviceType =
                SERVICE_TYPE
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
        if (currentServiceName.isNullOrEmpty()) {
            Timber.i("onServiceFound: NAME NOT SET")
            return
        }
        resolver.resolve(serviceInfo)
    }

    fun onServiceLost(nsdServiceInfo: NsdServiceInfo) {
        Timber.i("onServiceLost: $nsdServiceInfo")
        if (nsdServiceInfo.serviceName == currentServiceName) {
            Timber.i("onServiceLost: SELF")
            return
        }
//        resolvedNamesCache[nsdServiceInfo.serviceName]?.let {
//            client.removeClient(it)
//        }
    }
}