package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdServiceInfo
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class Client(private val receiverListener: (bytes: ByteArray) -> Unit) {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, Connection>()
    private val queueForDisconnecting = LinkedBlockingDeque<Connection>()
    private val lock = Object()
    private val lockCloseConnection = Object()
    private val lockRead = Object()

    fun addClient(socketAddress: InetSocketAddress) {
        Timber.i("addClient ${socketAddress.address.hostAddress}")
        synchronized(lock) {
            if (sockets[socketAddress.address.hostAddress] != null) {
                Timber.i("exist ${socketAddress.address.hostAddress}")
                // test connection - try send data if connection exist
                try {
                    sockets[socketAddress.address.hostAddress]?.socketChannel?.write(
                        ByteBuffer.wrap(
                            "ping".toByteArray()
                        )
                    )
                    return@synchronized
                } catch (e: Exception) {
                    try {
                        sockets[socketAddress.address.hostAddress]?.socketChannel?.finishConnect()
                        sockets[socketAddress.address.hostAddress]?.socketChannel?.close()
                    } catch (e: Exception) {
                        Timber.w(e)
                    }
                    Timber.w(e)
                }
            }
            Timber.i("added ${socketAddress.address.hostAddress}")
            executorService.submit() {
                try {
                    val socketChannel = SocketChannel.open(socketAddress)
                    //TODO select options
                    socketChannel.configureBlocking(false)
                    socketChannel.socket().keepAlive = true
                    socketChannel.socket().receiveBufferSize = 8192 * 4
                    sockets[socketAddress.address.hostAddress] = Connection(socketChannel, false)
                    startReading(socketAddress.address.hostAddress)
                } catch (e: Exception) {
                    Timber.w(socketAddress.address.hostAddress)
                    Timber.w(e)
                }
            }
        }
    }

    private fun startReading(serviceName: String) {
        Timber.i("startReading $serviceName")
        //TODO set correct buffer size
        val buffer = ByteBuffer.allocate(8192 * 8)
        do {
            val connection = sockets[serviceName]
            connection?.apply {
                if (!isPendingRemove) {
                    try {
                        if (socketChannel.isConnected) {
                            val readCount = socketChannel.read(buffer)
                            if (readCount > 0) {
                                buffer.flip()
                                read(buffer.array(), readCount)
                                buffer.compact()
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

    fun removeClient(nsdServiceInfo: NsdServiceInfo) {
        synchronized(lock) {
            try {
                sockets[nsdServiceInfo.serviceName]?.apply {
                    Timber.i("removeClient ${nsdServiceInfo.serviceName}")
                    try {
                        socketChannel.write(ByteBuffer.wrap("ping".toByteArray()))
                    } catch (e: Exception) {
                        Timber.w(e)
                        isPendingRemove = true
                        sockets[nsdServiceInfo.serviceName] = this
                        synchronized(lock) {
                            if (sockets[nsdServiceInfo.serviceName]?.isPendingRemove == true) {
                                sockets.remove(nsdServiceInfo.serviceName)
                            }
                        }
                        queueForDisconnecting.add(this)
                    }
                }
                closePendingConnections()
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    fun stop() {
        Timber.i("stop")
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int) {
        if (readCount > 20) {
            synchronized(lockRead) {
                receiverListener(byteArray)
            }
            Timber.i("message: audio $readCount")
        } else {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            Timber.i("message: ${String(rspData).trim()}")
        }
    }

    data class Connection(val socketChannel: SocketChannel, var isPendingRemove: Boolean)

}