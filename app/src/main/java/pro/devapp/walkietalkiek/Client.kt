package pro.devapp.walkietalkiek

import android.net.nsd.NsdServiceInfo
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class Client {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, SocketChannel>()

    fun addClient(addr: InetSocketAddress, nsdServiceInfo: NsdServiceInfo) {
        Timber.i("addClient ${addr.hostName}")
        synchronized(this) {
            if (sockets[nsdServiceInfo.serviceName]?.isConnected == true) {
                Timber.i("exist ${addr.hostName}")
                return@synchronized
            }
            Timber.i("added ${addr.hostName}")
            executorService.execute() {
                val socketChannel = SocketChannel.open(addr)
                sockets.put(nsdServiceInfo.serviceName, socketChannel)
                val buffer = ByteBuffer.allocate(256)
                //TODO need synchronization
                while (sockets[nsdServiceInfo.serviceName] != null) {
                    try {
                        if (socketChannel.isConnected) {
                            val readCount = socketChannel.read(buffer)
                            if (readCount > 0) {
                                buffer.flip()
                                read(buffer.array(), readCount)
                            }
                            buffer.clear()
                        } else {
                            socketChannel.close()
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        socketChannel.close()
                        buffer.clear()
                        break
                    }
                }

            }
        }
    }

    fun removeClient(nsdServiceInfo: NsdServiceInfo) {
        Timber.i("removeClient ${nsdServiceInfo.serviceName}")
        synchronized(this) {
            try {
                sockets[nsdServiceInfo.serviceName]?.apply {
                    Timber.i("removeClient ${nsdServiceInfo.serviceName}")
                    finishConnect()
                    close()
                    sockets.remove(nsdServiceInfo.serviceName)
                }
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    fun stop() {
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int) {
        executorServiceReader.execute {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            Timber.i("message: ${String(rspData).trim()}")
        }
    }

}