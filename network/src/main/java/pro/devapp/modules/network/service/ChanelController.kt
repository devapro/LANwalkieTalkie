package pro.devapp.modules.network.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import pro.devapp.modules.storage.ConnectedDevicesRepository
import pro.devapp.modules.storage.DeviceInfoRepository
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val connectedDevicesRepository: ConnectedDevicesRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)
    private val resolvedNamesCache = HashMap<String, String>()

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()
    private val executorPing = Executors.newSingleThreadScheduledExecutor()

    private val compositeDisposable = CompositeDisposable()
    val subjectAudioData = PublishSubject.create<ByteArray>()

    //  private val voicePlayer = VoicePlayer()

    private val isRegistered = AtomicBoolean(false)

    private val client = SocketClient().apply {
        clientConnectionSubject.subscribe {
            connectedDevicesRepository.addOrUpdateHostStateToConnected(it)
        }.also {
            compositeDisposable.add(it)
        }
        clientDisconnectionSubject.subscribe {
            connectedDevicesRepository.setHostDisconnected(it)
        }.also {
            compositeDisposable.add(it)
        }
    }
    private val server = SocketServer() { hostAddress, data ->
        if (data.size > 20) {
            subjectAudioData.onNext(data)
            Timber.i("message: audio ${data.size} from $hostAddress")
        } else {
            val message = String(data).trim()
            Timber.i("message: $message from $hostAddress")
        }
        connectedDevicesRepository.storeDataReceivedTime(hostAddress)
    }
        .apply {
            clientConnectionSubject.subscribe(object : Observer<InetSocketAddress> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(address: InetSocketAddress) {
                    // try connect to new client
                    client.addClient(address, false)
                    connectedDevicesRepository.addOrUpdateHostStateToConnected(address.address.hostAddress)
                }

                override fun onError(e: Throwable) {}

                override fun onComplete() {}
            })

            clientDisconnectionSubject.subscribe(object : Observer<InetSocketAddress> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(address: InetSocketAddress) {
                    client.removeClient(address.address.hostAddress)
                    connectedDevicesRepository.setHostDisconnected(address.address.hostAddress)
                }

                override fun onError(e: Throwable) {}

                override fun onComplete() {}
            })
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
        nsdManager.apply {
            val port = server.initServer()
            registerService(port)
        }
    }

    fun stopDiscovery() {
        if (isRegistered.get()) {
            nsdManager.apply {
                stopServiceDiscovery(discoveryListener)
                unregisterService(registrationListener)
            }
        }
        compositeDisposable.clear()
        executor.shutdown()
        executorPing.shutdown()
        client.stop()
        server.stop()
    }

    fun onServiceRegister() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        isRegistered.set(true)
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