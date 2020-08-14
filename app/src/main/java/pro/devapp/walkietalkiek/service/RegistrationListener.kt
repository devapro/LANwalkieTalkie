package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import pro.devapp.walkietalkiek.ChanelController
import java.lang.String
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.Executors


class RegistrationListener(private val chanelController: ChanelController) :
    NsdManager.RegistrationListener {
    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Log.e(
            "RegistrationListener",
            ": onRegistrationFailed: $serviceInfo ($errorCode)"
        )
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        Log.i(
            "RegistrationListener",
            ": onServiceUnregistered: $serviceInfo"
        )
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Log.e(
            "RegistrationListener",
            ": onUnregistrationFailed: $serviceInfo ($errorCode)"
        )
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Log.i(
            "RegistrationListener",
            ": onServiceRegistered: $serviceInfo"
        )
        //TODO check duplicate registration
        // after we can start connect to other services found
        //onServiceRegistered: name: SFVBV0VJIERVQS1MWDkgMTAgMjk:9dTeyFnNgNo:, type: null, host: null, port: 0, txtRecord:  &&&
        //chanelController.resolveService(serviceInfo)

        //  chanelController.acceptConnection()

   //     handleConnection(serviceInfo.port)
        //  handlerConnectionSocket()
    }

    private fun handlerConnectionSocket() {
        val selector = SelectorProvider.provider().openSelector()
        chanelController.serverSocketChannel?.register(selector, SelectionKey.OP_ACCEPT)
        val executor = Executors.newCachedThreadPool()
        executor.execute() {
            selector.selectNow()
            val it = selector.selectedKeys().iterator()
            while (it.hasNext()) {
                val key = it.next()
                it.remove();
                if (!key.isValid()) {
                    continue;
                }

                // Finish connection in case of an error
                if (key.isConnectable()) {
                    val ssc = key.channel() as SocketChannel
                    if (ssc.isConnectionPending()) {
                        ssc.finishConnect()
                    }
                }

                if (key.isAcceptable()) {
                    val ssc =
                        key.channel() as ServerSocketChannel
                    val newClient = ssc.accept()
                    newClient.configureBlocking(false)
                    newClient.register(selector, SelectionKey.OP_READ)
                    // sockets.add(newClient)
                    println("new client: " + newClient.socket().inetAddress.hostAddress)
                }

                if (key.isReadable()) {
                    val sc =
                        key.channel() as SocketChannel
                    val data = ByteBuffer.allocate(sc.socket().sendBufferSize)
                    println("new message: " + sc.socket().inetAddress.hostAddress)
                    if (sc.read(data) === -1) {
                        continue
                    }
                    data.flip()
                    println("message: $data")
                    sc.close()
                }

                if (key.isWritable) {
                    val sc = key.channel() as SocketChannel
                    val buf = ByteBuffer.wrap("test".toByteArray());
                    sc.write(buf);

                    println("send: test")
                    sc.close()

                    //key.interestOps(SelectionKey.OP_READ)
                }
            }
        }

    }

    private fun handleConnection(port: Int) {
        val selector = Selector.open();
        val datagramChannel = DatagramChannel.open()
        datagramChannel.configureBlocking(false)
        datagramChannel.socket().setReuseAddress(true)
        datagramChannel.register(selector, SelectionKey.OP_READ)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            datagramChannel.bind(InetSocketAddress(port))
        }

        val executor = Executors.newCachedThreadPool()
        executor.execute() {
            while (selector.isOpen) {
                selector.selectNow()
                val keys = selector.selectedKeys().iterator()
                while (keys.hasNext()) {
                    val key = keys.next();

                    try {
                        if (key.isReadable()) {
                            handleRead(key);
                        }

                        if (key.isValid() && key.isWritable()) {
                            handleWrite(key);
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    keys.remove();
                }
            }
        }
    }

    private val byteBuffer: ByteBuffer = ByteBuffer.allocate(256)
    private var from: SocketAddress? = null
    private fun handleRead(key: SelectionKey) {
        val channel =
            key.channel() as DatagramChannel

        byteBuffer.clear()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            channel.read(byteBuffer)
            from = channel.remoteAddress
        } else {
            from = channel.receive(byteBuffer)
        }
        byteBuffer.flip()

        Log.d("SOCKET", String.format("Received %d bytes from %s", byteBuffer.limit(), from))
        //println(String.format("Received %d bytes from %s", byteBuffer.limit(), from))

        key.interestOps(SelectionKey.OP_WRITE)
    }

    private fun handleWrite(key: SelectionKey) {
        val channel =
            key.channel() as DatagramChannel

        if (from != null) {
            byteBuffer.clear()
            byteBuffer.putInt(1234)
            byteBuffer.flip()
            val bytes = channel.send(byteBuffer, from)
            Log.d("SOCKET", String.format("Send %d bytes to %s", bytes, from))
            //    println(String.format("Send %d bytes to %s", bytes, from))
        }

        key.interestOps(SelectionKey.OP_READ)
    }
}