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


class ChanelController(
    private val context: Context,
    private val deviceInfoRepository: DeviceInfoRepository
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(this)
    private val registrationListener = RegistrationListener(this)
    private val resolveListener = ResolveListener(this)

    var serverSocketChannel: ServerSocketChannel? = null

    var currentServiceName: String? = null
        private set

    companion object {
        const val SERVICE_TYPE = "_wfwt._tcp" /* WiFi Walkie Talkie */ /* WiFi Walkie Talkie */
        const val LOG_TAG = "ChanelController"
    }

    fun startDiscovery() {
        nsdManager.apply {
            discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            registerService()
        }
    }

    fun stopDiscovery() {
        nsdManager.apply {
            stopServiceDiscovery(discoveryListener)
            unregisterService(registrationListener)
        }
    }


    private fun registerService() {
        //init server
        val selector = SelectorProvider.provider().openSelector()
        serverSocketChannel = ServerSocketChannel.open()

        // https://stackoverflow.com/questions/16825403/android-serversocketchannel-binding-to-loopback-address
        // "::0",
        val address = InetSocketAddress(6543)
        val socket = serverSocketChannel?.socket()
        //  socket?.reuseAddress = true
        //socket
        socket?.bind(address)
        serverSocketChannel?.configureBlocking(false)
        val selectKy = serverSocketChannel?.register(
            selector,
            serverSocketChannel!!.validOps(),
            null
        )//SelectionKey.OP_ACCEPT

        val executor = Executors.newCachedThreadPool()
        executor.execute() {
            while (true) {
                selector.select()
                val it = selector.selectedKeys().iterator()
                while (it.hasNext()) {
                    val key = it.next()
                    it.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    // Finish connection in case of an error
//                if (key.isConnectable()) {
//                    val ssc = key.channel() as SocketChannel
//                    if (ssc.isConnectionPending()) {
//                        ssc.finishConnect()
//                    }
//                }

                    if (key.isAcceptable()) {
                        val newClient = serverSocketChannel?.accept()

//                    val ssc = key.channel() as ServerSocketChannel
//                    val newClient = ssc.accept()
                        newClient?.configureBlocking(false)
                        newClient?.register(selector, SelectionKey.OP_READ)
                        // sockets.add(newClient)
                        println("new client: " + newClient?.socket()?.inetAddress?.hostAddress)
                    }

                    if (key.isReadable()) {
                        val sc =
                            key.channel() as SocketChannel
                        val buffer = ByteBuffer.allocate(sc.socket().sendBufferSize)
                        println("new message: " + sc.socket().inetAddress.hostAddress)
                        sc.read(buffer)
//                       if (sc.read(buffer) === -1) {
//                           continue
//                       }
                        //     buffer.flip()
                        println("message: ${String(buffer.array()).trim()}")
                        // sc.close()
                    }

//                if(key.isWritable){
//                    val sc = key.channel() as SocketChannel
//                    val buf = ByteBuffer.wrap("test".toByteArray());
//                    sc.write(buf);
//
//                    println("send: test")
//                    sc.close()
//
//                    //key.interestOps(SelectionKey.OP_READ)
//                }
                }
            }
        }

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

    fun acceptConnection() {
//        val executor = Executors.newCachedThreadPool()
//        executor.execute(){
//            while (true){
//                serverSocketChannel?.accept()
//                sleep(1000)
//            }
//        }
    }

    fun resolveService(serviceInfo: NsdServiceInfo) {
        //TODO add to quie then get from it and resolve sequinse
        nsdManager.resolveService(serviceInfo, resolveListener)
    }
}