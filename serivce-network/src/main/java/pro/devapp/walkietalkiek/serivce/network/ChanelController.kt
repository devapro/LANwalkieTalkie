package pro.devapp.walkietalkiek.serivce.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository
import timber.log.Timber
import java.nio.ByteBuffer

private const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */

class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val client: SocketClient,
    private val server: SocketServer,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val clientInfoResolver: ClientInfoResolver
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)

    private var currentServiceName: String? = null

    private var pingScope: CoroutineScope? = null

    fun startDiscovery() {
        pingScope = coroutineContextProvider.createScope(
            coroutineContextProvider.io
        )
        val port = server.initServer()
        registerNsdService(port)
    }

    fun stopDiscovery() {
        nsdManager.apply {
            stopServiceDiscovery(discoveryListener)
            unregisterService(registrationListener)
        }
        client.stop()
        server.stop()
        pingScope?.cancel()
    }

    fun onServiceRegister() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

//    fun sendMessage(byteBuffer: ByteBuffer) {
//        client.sendMessage(byteBuffer)
//    }

    private fun registerNsdService(port: Int) {
        Timber.Forest.i("registerService")
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
            Timber.Forest.i("try register $name: $serviceInfo")
            nsdManager.registerService(
                serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener
            )
            pingScope?.launch {
                while (isActive) {
                    ping()
                    delay(2000L)
                }
            }
        }
    }

    private fun ping() {
        server.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
        client.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
    }

    fun onServiceFound(serviceInfo: NsdServiceInfo) {
        // check for self add to list
        Timber.Forest.i("onServiceFound: ${serviceInfo.serviceName} current: $currentServiceName")
        if (serviceInfo.serviceName == currentServiceName) {
            Timber.Forest.i("onServiceFound: SELF")
            return
        }
        if (currentServiceName.isNullOrEmpty()) {
            Timber.Forest.i("onServiceFound: NAME NOT SET")
            return
        }

        clientInfoResolver.resolve(serviceInfo) { inetSocketAddress, nsdServiceInfo ->
            Timber.Forest.i("Resolve: ${nsdServiceInfo.serviceName}")
            connectedDevicesRepository.addHostInfo(
                inetSocketAddress.address.hostAddress,
                nsdServiceInfo.serviceName
            )
            client.addClient(inetSocketAddress, true)
        }
    }

    fun onServiceLost(nsdServiceInfo: NsdServiceInfo) {
        Timber.Forest.i("onServiceLost: $nsdServiceInfo")
        nsdServiceInfo.hostAddresses.firstOrNull()?.hostAddress?.let {
            connectedDevicesRepository.setHostDisconnected(it)
        }

        if (nsdServiceInfo.serviceName == currentServiceName) {
            Timber.Forest.i("onServiceLost: SELF")
            return
        }
    }
}