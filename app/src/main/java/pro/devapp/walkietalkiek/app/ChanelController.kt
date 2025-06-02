package pro.devapp.walkietalkiek.app

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.app.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.app.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val client: SocketClient,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)
    private val resolvedNamesCache = HashMap<String, String>()

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()
    private val executorPing = Executors.newSingleThreadScheduledExecutor()

    private val jobs = mutableListOf<Job>()
    private val coroutineScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )
    val subjectAudioData = MutableSharedFlow<ByteArray>()

    private val voicePlayer = VoicePlayer()

    private val server = SocketServer { hostAddress, data ->
        if (data.size > 20) {
            voicePlayer.play(data)
            subjectAudioData.tryEmit(data)
            Timber.i("message: audio ${data.size} from $hostAddress")
        } else {
            val message = String(data).trim()
            Timber.i("message: $message from $hostAddress")
        }
        connectedDevicesRepository.storeDataReceivedTime(hostAddress)
    }
        .apply {
            coroutineScope.launch {
                clientConnectionSubject.collect { address ->
                    client.addClient(address, false)
                    connectedDevicesRepository.addOrUpdateHostStateToConnected(address.address.hostAddress)
                }
            }.also {
                jobs.add(it)
            }
            coroutineScope.launch {
                clientDisconnectionSubject.collect { address ->
                    client.removeClient(address.address.hostAddress)
                    connectedDevicesRepository.setHostDisconnected(address.address.hostAddress)
                }
            }.also {
                jobs.add(it)
            }
        }

    private val resolver =
        Resolver(nsdManager) { inetSocketAddress, nsdServiceInfo ->
            Timber.i("Resolve: ${nsdServiceInfo.serviceName}")
            resolvedNamesCache[nsdServiceInfo.serviceName] = inetSocketAddress.address.hostAddress
            connectedDevicesRepository.addHostInfo(
                inetSocketAddress.address.hostAddress,
                nsdServiceInfo.serviceName
            )
            client.addClient(inetSocketAddress)
        }

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */
    }

    fun startDiscovery() {
        coroutineScope.launch {
            client.clientConnectionSubject.collect {
                connectedDevicesRepository.addOrUpdateHostStateToConnected(it)
            }
        }.also {
            jobs.add(it)
        }
        coroutineScope.launch {
            client.clientDisconnectionSubject.collect {
                connectedDevicesRepository.setHostDisconnected(it)
            }
        }.also {
            jobs.add(it)
        }
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
        jobs.forEach {
            if (it.isActive) {
                it.cancel()
            }
        }
        executor.shutdown()
        executorPing.shutdown()
        client.stop()
        server.stop()
        voicePlayer.stopPlay()
    }

    fun onServiceRegister() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        client.sendMessage(byteBuffer)
    }

    private fun registerService(port: Int) {
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
            executorPing.scheduleWithFixedDelay({ ping() }, 1000, 2000, TimeUnit.MILLISECONDS)
        }
    }

    private fun ping() {
        server.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
        client.sendMessage(ByteBuffer.wrap("ping".toByteArray()))
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
        resolvedNamesCache[nsdServiceInfo.serviceName]?.let {
            client.removeClient(it)
        }
    }
}