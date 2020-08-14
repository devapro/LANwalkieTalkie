package pro.devapp.walkietalkiek

import android.util.Log
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class Clients {
    private val executorService = Executors.newCachedThreadPool()

    companion object {
        const val LOG_TAG = "Clients"
    }

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

    fun addClient(addr: InetSocketAddress) {
        Log.d(LOG_TAG, "addClient")

        executorService.execute() {
            val socketChannel = SocketChannel.open(addr)
            val buffer = ByteBuffer.allocate(socketChannel.socket().receiveBufferSize)
            //   val buffer = ByteBuffer.wrap(String("test").bytes)
            while (true) {
                try {
                    if (socketChannel.isConnected) {
                        // socketChannel.write(buffer)
                        val readCount = socketChannel.read(buffer)
                        if (readCount > 0) {
                            buffer.flip()
                            val rspData = ByteArray(readCount)
                            System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
                            println("message: ${kotlin.text.String(rspData).trim()}")
                            //TODO
                            //  sendMessage(ByteBuffer.wrap("received".toByteArray()))
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
                    break
                }
            }

        }
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

    fun stop() {
        executorService.shutdown()
    }

}