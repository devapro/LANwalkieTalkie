package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class Server(private val connectionListener: ConnectionListener) {
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
            serverSocketChannel.validOps(),
            null
        )//SelectionKey.OP_ACCEPT
        executorService.execute() {
            while (true) {
                selector.select()
                val it = selector.selectedKeys().iterator()
                var connectionCount = selector.selectedKeys().size

                val f = outputQueue.pollFirst()
                val buf = f?.run { ByteBuffer.wrap(array()) }
                buf?.let { Timber.i("wait to send ${buf.array()[0]}") }
                while (it.hasNext()) {
                    connectionCount--
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
                            newClient.apply {
                                configureBlocking(false)
                                //TODO ???
                                socket().sendBufferSize = 8192 * 2
                                register(selector, SelectionKey.OP_WRITE)
                                val host = newClient.socket().inetAddress.hostAddress
                                Timber.i("isAcceptable $host")
                                connectionListener.onNewClient(
                                    InetSocketAddress(
                                        host,
                                        socket().port
                                    )
                                )
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
                            } else if (buf != null) {
                                try {
                                    while (buf.hasRemaining()) {
                                        sc.write(buf)
                                    }
                                    Timber.i("send: ${buf.array().size} to ${sc.socket().inetAddress.hostName}")
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

    interface ConnectionListener {
        fun onNewClient(address: InetSocketAddress)
    }
}