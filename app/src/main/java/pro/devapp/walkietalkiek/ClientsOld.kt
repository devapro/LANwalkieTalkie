package pro.devapp.walkietalkiek

import android.util.Log
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

class ClientsOld {
    private val selector = Selector.open()
    private val executorService = Executors.newCachedThreadPool()

    companion object {
        const val LOG_TAG = "Clients"
    }

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()
    private val isClientRun = AtomicBoolean(false)
    private val clients = HashMap<String, SocketChannel>()

    fun addClient(addr: InetSocketAddress) {
        Log.d(LOG_TAG, "addClient")
        executorService.execute() {
            try {
                clients[addr.hostName]?.apply {
                    if (!isConnected) {
                        finishConnect()
                        close()
                    }
                    Log.d(LOG_TAG, "close old connection")
                }
                val socketChannel = SocketChannel.open(addr)

                socketChannel.configureBlocking(false)
                selector.wakeup()
                val sectionKey = socketChannel.register(selector, 0, null)
                Log.d(LOG_TAG, "register")
                val interestOps = sectionKey.interestOps()
                sectionKey.interestOps(interestOps or SelectionKey.OP_READ)
                clients.put(addr.hostName, socketChannel)
                //
                initClient()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

    fun stop() {
        executorService.shutdown()
    }

    private fun initClient() {
        if (isClientRun.compareAndSet(false, true)) {
            try {
                while (selector.isOpen) {
                    selector.select()
                    val it = selector.selectedKeys().iterator()
                    while (it.hasNext()) {
                        val key = it.next()
                        it.remove();
                        if (!key.isValid) {
                            continue;
                        }

                        // Finish connection in case of an error
                        if (key.isConnectable) {
                            val ssc = key.channel() as SocketChannel
                            if (ssc.isConnectionPending) {
                                ssc.finishConnect()
                            }
                        }

                        if (key.isReadable) {
                            val sc = key.channel() as SocketChannel
                            if (!sc.isConnected) {
                                key.channel().close()
                                key.cancel()
                                continue
                            }
                            val buffer = ByteBuffer.allocate(sc.socket().receiveBufferSize)

                            val readCount = try {
                                sc.read(buffer)
                            } catch (e: Exception) {
                                -1
                            }
                            if (readCount == -1) {
                                key.channel().close()
                                key.cancel()
                                continue
                            }
                            if (readCount > 0) {
                                println("new message: " + sc.socket().inetAddress.hostAddress)
                                val rspData = ByteArray(readCount)
                                System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
                                //  buffer.flip()
                                println("message: ${java.lang.String(rspData).trim()}")

                                if (outputQueue.isNotEmpty()) {
                                    val outputBuff = outputQueue.pollFirst()
                                    outputBuff?.let {
                                        sc.write(it);
                                    }
                                }

                                //TODO
                                sendMessage(ByteBuffer.wrap("received".toByteArray()))
                            }

                            //  key.interestOps(SelectionKey.OP_WRITE)
                        }

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isClientRun.set(false)
    }
}