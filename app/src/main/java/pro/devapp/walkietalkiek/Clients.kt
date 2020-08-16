package pro.devapp.walkietalkiek

import android.net.nsd.NsdServiceInfo
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class Clients {
    private val executorService = Executors.newCachedThreadPool()
    private val sockets = HashMap<String, SocketChannel>()

    companion object {
        const val LOG_TAG = "Clients"
    }

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

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
                //   val buffer = ByteBuffer.wrap(String("test").bytes)
                while (sockets[nsdServiceInfo.serviceName] != null) {
                    try {
                        if (socketChannel.isConnected) {
                            val readCount = socketChannel.read(buffer)
                            if (readCount > 0) {
                                buffer.flip()
                                read(buffer.array(), readCount)
                            }
                            buffer.clear()

//                        if (outputQueue.isNotEmpty()) {
//                            val outputBuff = outputQueue.pollFirst()
//                            outputBuff?.let {
//                                socketChannel.write(buffer)
//                            }
//                        }

                        } else {
                            socketChannel.close()
                            break
                        }
                        //    buffer.clear()
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
            sockets[nsdServiceInfo.serviceName]?.apply {
                Timber.i("removeClient ${nsdServiceInfo.serviceName}")
                finishConnect()
                close()
                sockets.remove(nsdServiceInfo.serviceName)
            }
        }
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

    fun stop() {
        executorService.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int) {
        executorService.execute {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            println("message: ${String(rspData).trim()}")

            //sendMessage(ByteBuffer.wrap("received".toByteArray()))
        }
    }

}