package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import kotlin.collections.HashMap

class Client(private val receiverListener: (bytes: ByteArray) -> Unit) : IClient {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, IClient.Connection>()
    private val queueForDisconnecting = LinkedBlockingDeque<IClient.Connection>()
    private val lock = Object()
    private val lockCloseConnection = Object()
    private val lockRead = Object()

    override fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean) {
        Timber.i("addClient ${socketAddress.address.hostAddress}")
        synchronized(lock) {
            if (sockets[socketAddress.address.hostAddress] != null) {
                Timber.i("exist ${socketAddress.address.hostAddress}")
                return
                // test connection - try send data if connection exist
//                try {
//                    sockets[socketAddress.address.hostAddress]?.socketChannel?.write(
//                        ByteBuffer.wrap(
//                            "ping".toByteArray()
//                        )
//                    )
//                    return@synchronized
//                } catch (e: Exception) {
//                    try {
//                        sockets[socketAddress.address.hostAddress]?.socketChannel?.finishConnect()
//                        sockets[socketAddress.address.hostAddress]?.socketChannel?.close()
//                    } catch (e: Exception) {
//                        Timber.w(e)
//                    }
//                    Timber.w(e)
//                }

            }
            Timber.i("try add ${socketAddress.address.hostAddress}")
            executorService.submit() {
                try {
                    val socketChannel = SocketChannel.open(socketAddress)
                    //TODO select options
                    socketChannel.configureBlocking(false)
                    // socketChannel.socket().keepAlive = true
                    socketChannel.socket().receiveBufferSize = 8192 * 4
                    sockets[socketAddress.address.hostAddress] =
                        IClient.Connection(socketChannel, false)
                    Timber.i("added1 ${socketAddress.address.hostAddress}")
                    startReading(socketAddress.address.hostAddress)

                } catch (e: Exception) {
                    Timber.w(socketAddress.address.hostAddress)
                    Timber.w(e)
                }
            }
            Timber.i("added2 ${socketAddress.address.hostAddress}")
        }
    }

    override fun removeClient(hostAddress: String) {

    }

    private fun startReading(serviceName: String) {
        Timber.i("startReading $serviceName")
        //TODO set correct buffer size
        val buffer = ByteBuffer.allocate(8192 * 8)
        var lastPingAt = Date().time
        do {
            val connection = sockets[serviceName]
            connection?.apply {
                if (!isPendingRemove) {
                    try {
                        if (socketChannel.isConnected) {
                            val readCount = socketChannel.read(buffer)
                            if (readCount > 0) {
                                buffer.flip()
                                read(buffer.array(), readCount, serviceName)
                                buffer.compact()
                            } else if (readCount < 0) {
                                isPendingRemove = true
                            } else {
                                val currentTime = Date().time
                                if (currentTime - lastPingAt > 10000) {
                                    socketChannel.write(ByteBuffer.wrap("ping".toByteArray()))
                                    lastPingAt = currentTime
                                    Timber.i("sent ping to $serviceName")
                                }
                            }
                            java.util.Arrays.fill(buffer.array(), 0)
                            buffer.clear()
                        } else {
                            Timber.i("socket chanel not connected")
                            isPendingRemove = true
                        }
                    } catch (e: Exception) {
                        Timber.w(e)
                        isPendingRemove = !socketChannel.isConnected
                    }
                }
            }
        } while (connection != null && !connection.isPendingRemove)
        Timber.i("read data stop")
        val connection = sockets[serviceName]
        connection?.apply {
            synchronized(lock) {
                if (sockets[serviceName]?.isPendingRemove == true) {
                    sockets.remove(serviceName)
                }
            }
            queueForDisconnecting.add(this)
        }
        Timber.i("endReading $serviceName")
        closePendingConnections()
    }

    private fun closePendingConnections() {
        synchronized(lockCloseConnection) {
            do {
                val connection = queueForDisconnecting.pollFirst()
                connection?.apply {
                    Timber.i("close ${socketChannel.socket().inetAddress.hostName}")
                    socketChannel.finishConnect()
                    socketChannel.close()
                }
            } while (queueForDisconnecting.isNotEmpty())
        }
    }

//    override fun removeClient(nsdServiceInfo: NsdServiceInfo) {
//        synchronized(lock) {
//            try {
//                sockets[nsdServiceInfo.serviceName]?.apply {
//                    Timber.i("removeClient ${nsdServiceInfo.serviceName}")
//                    try {
//                        socketChannel.write(ByteBuffer.wrap("ping".toByteArray()))
//                    } catch (e: Exception) {
//                        Timber.w(e)
//                        isPendingRemove = true
//                        sockets[nsdServiceInfo.serviceName] = this
//                        synchronized(lock) {
//                            if (sockets[nsdServiceInfo.serviceName]?.isPendingRemove == true) {
//                                sockets.remove(nsdServiceInfo.serviceName)
//                            }
//                        }
//                        queueForDisconnecting.add(this)
//                    }
//                }
//                closePendingConnections()
//            } catch (e: Exception) {
//                Timber.w(e)
//            }
//        }
//    }

    override fun stop() {
        Timber.i("stop")
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int, serviceName: String) {
        if (readCount > 20) {
            synchronized(lockRead) {
                receiverListener(byteArray)
            }
            Timber.i("message: audio $readCount")
        } else {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            Timber.i("message: ${String(rspData).trim()} from $serviceName")
        }
    }

}