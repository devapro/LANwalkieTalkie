package pro.devapp.walkietalkiek

import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class Server {
    private val executorService = Executors.newCachedThreadPool()

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

    fun initServer(): Int {
        Timber.i("initServer")
        val selector = SelectorProvider.provider().openSelector()
        val serverSocketChannel = ServerSocketChannel.open()
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
        executorService.execute() {
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
                        key.isConnectable -> {
                            // Finish connection in case of an error
                            val ssc = key.channel() as SocketChannel
                            val host = ssc.socket().inetAddress.hostName
                            Timber.i("isConnectable $host")
//                            if (ssc.isConnectionPending) {
//                                ssc.finishConnect()
//                            }
                        }
                        key.isAcceptable -> {
                            val ssc = key.channel() as ServerSocketChannel
                            val newClient = ssc.accept()
                            val host = newClient.socket().inetAddress.hostName
                            Timber.i("isAcceptable $host")
                            newClient.apply {
                                configureBlocking(false)
                                register(selector, SelectionKey.OP_WRITE)
                                Timber.i("isAcceptable $host")
                            }
                        }
                        key.isReadable -> {
                            val ssc = key.channel() as SocketChannel
                            val host = ssc.socket().inetAddress.hostName
                            Timber.i("isReadable $host")
                            key.interestOps(SelectionKey.OP_WRITE)
                        }
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
                        }
                        else -> {
                            key.interestOps(SelectionKey.OP_WRITE)
                        }
                    }
                }
            }
        }
        return serverSocketChannel.socket().localPort
    }

    fun stop() {
        executorService.shutdown()
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }
}