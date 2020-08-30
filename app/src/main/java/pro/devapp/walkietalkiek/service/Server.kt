package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import kotlin.collections.HashMap

class Server(private val connectionListener: IServer.ConnectionListener) : IServer {

    companion object {
        const val SERVER_PORT = 6543
    }

    private val executorService = Executors.newCachedThreadPool()

    /**
     * Data for sending
     */
    private val outputQueue = LinkedList<ByteBuffer>()

    private val outputQueueMap = HashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    override fun initServer(): Int {
        Timber.i("initServer")
        val buffer = ByteBuffer.allocate(256)
        val selector = SelectorProvider.provider().openSelector()
        val serverSocketChannel = ServerSocketChannel.open()
        // https://stackoverflow.com/questions/16825403/android-serversocketchannel-binding-to-loopback-address
        val address = InetSocketAddress(SERVER_PORT)
        Timber.i(address.address.hostAddress)
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
            var lastSentTime = Date().time
            while (true) {
                selector.select()
                val it = selector.selectedKeys().iterator()
                //   var connectionCount = selector.selectedKeys().size

                val buf = outputQueue.pollFirst()
                buf?.let {
                    Timber.i("wait to send ${buf.array()[0]}")
                    outputQueueMap.forEach { item ->
                        item.value.add(buf)
                    }
                }

                while (it.hasNext()) {
                    //     connectionCount--
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
                            if (ssc.isConnectionPending) {
                                ssc.finishConnect()
                            }
                        }
                        key.isAcceptable -> {
                            val ssc = key.channel() as ServerSocketChannel
                            val newClient = ssc.accept()
                            newClient.apply {
                                configureBlocking(false)
                                //TODO ???
                                socket().sendBufferSize = 8192 * 2
                                register(selector, SelectionKey.OP_WRITE)
                                val hostAddress = newClient.socket().inetAddress.hostAddress
                                Timber.i("isAcceptable $hostAddress")
                                connectionListener.onNewClient(
                                    InetSocketAddress(
                                        hostAddress,
                                        socket().port
                                    )
                                )
                                outputQueueMap[hostAddress] = LinkedBlockingDeque<ByteBuffer>()
                            }
                        }
                        key.isReadable -> {
                            val ssc = key.channel() as SocketChannel
                            val host = ssc.socket().inetAddress.hostName
                            buffer.clear()
                            ssc.read(buffer)
                            buffer.flip()
                            Timber.i("isReadable $host ${String(buffer.array())}")
                            try {
                                if (outputQueue.isEmpty()) {
                                    ssc.write(ByteBuffer.wrap("pong".toByteArray()))
                                }
                                key.interestOps(SelectionKey.OP_WRITE)
                            } catch (e: Exception) {
                                Timber.w(e)
                                ssc.close()
                            }
                        }
                        key.isWritable -> {
                            val sc = key.channel() as SocketChannel
                            val hostAddress = sc.socket().inetAddress.hostAddress
                            val bufToSend = outputQueueMap[hostAddress]?.pollFirst()
                            if (sc.isConnectionPending || !sc.isConnected) {
                                sc.finishConnect()
                            } else if (bufToSend != null) {
                                try {
//                                    while (bufToSend.hasRemaining()) {
//                                        sc.write(bufToSend)
//                                    }
                                    sc.write(bufToSend)
                                    Timber.i("send: ${bufToSend.array().size} to ${sc.socket().inetAddress.hostName}")
                                    lastSentTime = Date().time
                                } catch (e: Exception) {
                                    Timber.w(e)
                                    sc.finishConnect()
                                    sc.close()
                                }
                            } else {
                                val currentTime = Date().time
                                if (currentTime - lastSentTime > 5000) {
                                    //key.interestOps(SelectionKey.OP_READ)
                                    lastSentTime = currentTime
                                    buffer.clear()
                                    val readCount = sc.read(buffer)
                                    buffer.flip()
                                    if (readCount > 0) {
                                        Timber.i("isReadable $hostAddress ${String(buffer.array())}")
                                        try {
                                            if (outputQueue.isEmpty()) {
                                                sc.write(ByteBuffer.wrap("pong".toByteArray()))
                                            }
                                            // key.interestOps(SelectionKey.OP_WRITE)
                                        } catch (e: Exception) {
                                            Timber.w(e)
                                            sc.close()
                                        }
                                    }
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

    override fun stop() {
        executorService.shutdown()
    }

    override fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

}