package pro.devapp.walkietalkiek

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import android.util.Log
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.DiscoveryListener
import pro.devapp.walkietalkiek.service.RegistrationListener
import pro.devapp.walkietalkiek.service.ResolveListener
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque


class ChanelController(
    private val context: Context,
    private val deviceInfoRepository: DeviceInfoRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)
    private val resolveListener = ResolveListener(this)

    var serverSocketChannel: ServerSocketChannel? = null

    private var currentServiceName: String? = null

    private val executor = Executors.newCachedThreadPool()

    private val client = Clients()

    private val resolver = Resolver(nsdManager) {
        initClient(it)
    }

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */ /* WiFi Walkie Talkie */
        const val LOG_TAG = "ChanelController"
    }

    fun startDiscovery() {
        nsdManager.apply {
            initServer()
            registerService()
            discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
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

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

    private fun initServer() {
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
        //    val buffer = ByteBuffer.allocate(socket?.receiveBufferSize ?: 256)
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

                    // Finish connection in case of an error
//                    if (key.isConnectable()) {
//                        val ssc = key.channel() as SocketChannel
//                        if (ssc.isConnectionPending()) {
//                            ssc.finishConnect()
//                        }
//                    }

                    if (key.isAcceptable) {
                        val ssc = key.channel() as ServerSocketChannel
                        val newClient = ssc.accept()
                        newClient?.apply {
                            configureBlocking(false)
                            register(selector, SelectionKey.OP_WRITE)
                            println("new client: " + socket().inetAddress.hostName)
                        }
                    }

//                    if (key.isReadable) {
//                        val sc = key.channel() as SocketChannel
//                        val buffer = ByteBuffer.allocate(sc.socket().receiveBufferSize)
//                        println("new message: " + sc.socket().inetAddress.hostAddress)
//                        val readCount = sc.read(buffer)
////                        if (readCount == -1) {
////                            key.channel().close()
////                            key.cancel()
////                            continue
////                        }
//                        if(readCount > 0){
//                            val rspData = ByteArray(readCount)
//                            System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
//                            //  buffer.flip()
//                            println("message: ${String(rspData).trim()}")
//                        }
//
//                        key.interestOps(SelectionKey.OP_WRITE)
//                    }

                    if (key.isWritable) {
                        val sc = key.channel() as SocketChannel
                        if (sc.isConnectionPending || !sc.isConnected) {
                            sc.finishConnect()
                            continue
                        }
                        if (outputQueue.isNotEmpty()) {
                            try {
                                val buf =
                                    outputQueue.pollFirst() //ByteBuffer.wrap("test".toByteArray());
                                sc.write(buf);
                                println("send: ${buf.array().size}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                sc.finishConnect()
                                sc.close()
                            }
                        }
//                        if(outputQueue.isEmpty()){
//                            key.interestOps(SelectionKey.OP_READ)
//                        } else {
//                            key.interestOps(SelectionKey.OP_WRITE)
//                        }

//                        if (outputQueue.isEmpty()) {
//                            val readCount = sc.read(buffer)
//                            if (readCount > 0) {
//                                val rspData = ByteArray(readCount)
//                                System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
//                                //  buffer.flip()
//                                println("message: ${String(rspData).trim()}")
//                            }
//                        }
                    }
                }
            }
        }
    }

    private fun registerService() {
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
            Log.i(LOG_TAG, "$name: register service: $serviceInfo")
            nsdManager.registerService(
                serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener
            )
        }
        result.exceptionOrNull()?.apply { }
    }


    fun resolveService(serviceInfo: NsdServiceInfo) {
        // check for self add to list
        if (serviceInfo.serviceName == currentServiceName) {
            Log.d(LOG_TAG, "onServiceResolved: SELF")
            return
        }
        resolver.resolve(serviceInfo)

        //nsdManager.resolveService(serviceInfo, resolveListener)
    }

    private fun initClient(addr: InetSocketAddress) {
        client.addClient(addr)
    }
}