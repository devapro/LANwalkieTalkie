package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import pro.devapp.walkietalkiek.ChanelController
import java.lang.String
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors


class ResolveListener(private val chanelController: ChanelController) : NsdManager.ResolveListener {

    companion object {
        const val LOG_TAG = "ResolveListener"
    }

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Log.i(
            LOG_TAG,
            ": onResolveFailed: $serviceInfo errorCode=$errorCode"
        )
        //TODO remove from list
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.i(LOG_TAG, ": onServiceResolved: $serviceInfo");
        // check for self add to list
        if (serviceInfo.serviceName == chanelController.currentServiceName) {
            Log.d("ResolveListener", "onServiceResolved: SELF")
            return
        }
        //TODO add to list
        val addr = InetSocketAddress(serviceInfo.host, serviceInfo.port)
        Log.d("ResolveListener", "onServiceResolved: $addr")
        if (!addr.address.isMulticastAddress) {
            //handleConnection(addr)
            handleSocketConnection(addr)
        }
    }

    private val selector = Selector.open()
    private fun handleSocketConnection(addr: InetSocketAddress) {
        val socketChannel = SocketChannel.open(addr)
        //    socketChannel.configureBlocking( false )
//        val connected = socketChannel.connect(addr)
//        if(connected){
//
//        }
        val executor = Executors.newCachedThreadPool()
        executor.execute() {
//            val sectionKey = socketChannel.register( selector, 0, null )
//            val  interestOps = sectionKey.interestOps()
//            sectionKey.interestOps( interestOps or SelectionKey.OP_READ )
            while (true) {
//                    if(sectionKey.isReadable){
//                        val sc =
//                            sectionKey.channel() as SocketChannel
//                        val data = ByteBuffer.allocate(sc.socket().sendBufferSize)
//                        println("new message: " + sc.socket().inetAddress.hostAddress)
//                    }
                //socketChannel.read(byteBuffer)

                try {
                    val buffer = ByteBuffer.wrap("test".toByteArray())
                    if (socketChannel.isConnected) {
                        socketChannel.write(buffer)
                    }
                    buffer.clear()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.d("ResolveListener", "sending....")
                Thread.sleep(2000)
            }

        }
    }

    private fun handleConnection(addr: InetSocketAddress) {
        // https://stackoverflow.com/questions/31337387/datagramchannel-not-receiving-any-bytes-on-android
        // https://stackoverflow.com/questions/14690226/how-to-use-selector-object-with-datagramchannel-to-do-non-blocking-packet-recept
        val selector = Selector.open()
        val datagramChannel = DatagramChannel.open()
        val socket = datagramChannel.socket()
        datagramChannel.configureBlocking(false)
        socket.reuseAddress = true
        datagramChannel.register(selector, SelectionKey.OP_WRITE)
        datagramChannel.connect(addr)
        val executor = Executors.newCachedThreadPool()
        executor.execute() {
            try {
                while (selector.isOpen) {
                    selector.select()
                    val keys =
                        selector.selectedKeys().iterator()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.isReadable) {
                            handleRead(key)
                        }
                        if (key.isValid && key.isWritable) {
                            handleWrite(key, addr)
                        }
                        keys.remove()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!selector.isOpen) {
                executor.shutdown()
            }
        }
    }

    private val byteBuffer = ByteBuffer.allocate(256)
    private fun handleRead(key: SelectionKey) {
        val channel = key.channel() as DatagramChannel

        byteBuffer.clear()
        val from = channel.read(byteBuffer)
        byteBuffer.flip()

        Log.i(
            LOG_TAG,
            String.format("Received %d bytes from %s", byteBuffer.limit(), from)
        )

        key.interestOps(SelectionKey.OP_WRITE)
    }

    private fun handleWrite(key: SelectionKey, addr: InetSocketAddress) {
        val channel =
            key.channel() as DatagramChannel

        byteBuffer.clear()
        byteBuffer.putInt(1234)
        byteBuffer.flip()

        val bytes = channel.send(byteBuffer, addr)

        Log.i(LOG_TAG, kotlin.String.format("Send %d bytes to %s", bytes, addr))

        key.interestOps(SelectionKey.OP_READ)
    }
}