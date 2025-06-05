package pro.devapp.walkietalkiek.app

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val client: SocketClient,
    private val server: SocketServer,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val resolver: Resolver
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)

    private var currentServiceName: String? = null

    private val executorPing = Executors.newSingleThreadScheduledExecutor()

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */
    }

    fun startDiscovery() {
        val port = server.initServer()
        registerNsdService(port)
    }

    fun stopDiscovery() {
        nsdManager.apply {
            stopServiceDiscovery(discoveryListener)
            unregisterService(registrationListener)
        }
        executorPing.shutdown()
        client.stop()
        server.stop()
    }

    fun onServiceRegister() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        client.sendMessage(byteBuffer)
    }

    private fun registerNsdService(port: Int) {
        Timber.i("registerService")
        val result = deviceInfoRepository.getCurrentDeviceInfo()
        result.apply {
            // Android NSD implementation is very unstable when services
            // registers with the same name. Will use "CHANNEL_NAME:DEVICE_ID:".
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
            executorPing.scheduleWithFixedDelay({ ping() }, 1000, 2000, TimeUnit.MILLISECONDS)
        }
    }

    private fun ping() {
        server.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
        client.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
    }

    fun onServiceFound(serviceInfo: NsdServiceInfo) {
        // check for self add to list
        Timber.i("onServiceFound: ${serviceInfo.serviceName} current: $currentServiceName")
        if (serviceInfo.serviceName == currentServiceName) {
            Timber.i("onServiceFound: SELF")
            return
        }
        if (currentServiceName.isNullOrEmpty()) {
            Timber.i("onServiceFound: NAME NOT SET")
            return
        }

        resolver.resolve(serviceInfo) { inetSocketAddress, nsdServiceInfo ->
            Timber.i("Resolve: ${nsdServiceInfo.serviceName}")
            connectedDevicesRepository.addHostInfo(
                inetSocketAddress.address.hostAddress,
                nsdServiceInfo.serviceName
            )
            client.addClient(inetSocketAddress)
        }
    }

    fun onServiceLost(nsdServiceInfo: NsdServiceInfo) {
        Timber.i("onServiceLost: $nsdServiceInfo")
        connectedDevicesRepository.setHostDisconnected(nsdServiceInfo.host.hostAddress)
        if (nsdServiceInfo.serviceName == currentServiceName) {
            Timber.i("onServiceLost: SELF")
            return
        }
    }
}