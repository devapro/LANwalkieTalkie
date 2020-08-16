package pro.devapp.walkietalkiek

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.DiscoveryListener
import pro.devapp.walkietalkiek.service.RegistrationListener
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque


class ChanelController(
    context: Context,
    private val deviceInfoRepository: DeviceInfoRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)

    var serverSocketChannel: ServerSocketChannel? = null

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()

    private val client = Clients()

    private val resolver = Resolver(nsdManager) { addr, nsdServiceInfo ->
        client.addClient(addr, nsdServiceInfo)
    }

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */ /* WiFi Walkie Talkie */
    }

    fun startDiscovery() {
        nsdManager.apply {
            initServer()
            registerService()
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
        outputQueue.add(byteBuffer)
    }

    private fun initServer() {
        Timber.i("initServer")
        val selector = SelectorProvider.provider().openSelector()
        serverSocketChannel = ServerSocketChannel.open()
        // https://stackoverflow.com/questions/16825403/android-serversocketchannel-binding-to-loopback-address
        val address = InetSocketAddress(6543)
        val socket = serverSocketChannel?.socket()
        //  socket?.reuseAddress = true
        socket?.bind(address)
        serverSocketChannel?.configureBlocking(false)
        val selectKy = serverSocketChannel?.register(
            selector,
            serverSocketChannel!!.validOps(),
            null
        )//SelectionKey.OP_ACCEPT
        val buffer = ByteBuffer.allocate(socket?.receiveBufferSize ?: 256)
        executor.execute() {
            while (true) {
                selector.select()
                val it = selector.selectedKeys().iterator()
                while (it.hasNext()) {
                    val key = it.next()
                    it.remove();
                    if (!key.isValid) {
                        continue;
                    }

                    when {
//                        key.isConnectable -> {
//                            // Finish connection in case of an error
//                            val ssc = key.channel() as SocketChannel
//                            val host = ssc.socket().inetAddress.hostName
//                            Timber.i("isConnectable $host")
//                            if (ssc.isConnectionPending) {
//                                ssc.finishConnect()
//                            }
//                        }
                        key.isAcceptable -> {
                            val ssc = key.channel() as ServerSocketChannel
                            val newClient = ssc.accept()
                            val host = newClient.socket().inetAddress.hostName
                            Timber.i("isAcceptable $host")
                            newClient?.apply {
                                configureBlocking(false)
                                register(selector, SelectionKey.OP_WRITE)
                                Timber.i("isAcceptable $host")
                            }
                        }
//                        key.isReadable -> {
//                            val ssc = key.channel() as SocketChannel
//                            val host = ssc.socket().inetAddress.hostName
//                            try {
//                                val readCount = ssc.read(buffer)
//                                buffer.flip()
//    //                        if (readCount == -1) {
//    //                            key.channel().close()
//    //                            key.cancel()
//    //                            continue
//    //                        }
//                                if (readCount > 0) {
//                                    Timber.i("new message $host")
//                                    read(buffer.array(), readCount)
//                                }
//                            } catch (e: Exception){
//                                Timber.w(e)
//                            }
//                            key.interestOps(SelectionKey.OP_WRITE)
//                        }
                        key.isWritable -> {
                            val sc = key.channel() as SocketChannel
                            if (sc.isConnectionPending || !sc.isConnected) {
                                sc.finishConnect()
                            } else if (outputQueue.isNotEmpty()) {
                                try {
                                    val buf =
                                        outputQueue.pollFirst() //ByteBuffer.wrap("test".toByteArray());
                                    sc.write(buf)
                                    Timber.i("send: ${buf?.array()?.size}")
                                } catch (e: Exception) {
                                    Timber.w(e)
                                    sc.finishConnect()
                                    sc.close()
                                }
                            }
//                        if(outputQueue.isEmpty()){
//                            key.interestOps(SelectionKey.OP_READ)
//                        } else {
//                            key.interestOps(SelectionKey.OP_WRITE)
//                        }

//                        if (outputQueue.isEmpty() && sc.isOpen && sc.isConnected) {
//                            buffer.clear()
//                            try {
//                                val readCount = sc.read(buffer)
//                                buffer.flip()
//                                if (readCount > 0) {
//                                    read(buffer.array(), readCount)
//                                }
//                            } catch (e: Exception){
//
//                            }
//                            buffer.clear()
//                        }
                        }
                        else -> {
                            // key.interestOps(SelectionKey.OP_WRITE)
                        }
                    }
                }
            }
        }
    }

    private fun read(byteArray: ByteArray, readCount: Int) {
        executor.execute {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            Timber.i("message: ${String(rspData).trim()}")
            //TODO
            //  sendMessage(ByteBuffer.wrap("received".toByteArray()))
        }
    }

    private fun registerService() {
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
            serviceInfo.port = serverSocketChannel?.socket()?.localPort ?: 6543
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