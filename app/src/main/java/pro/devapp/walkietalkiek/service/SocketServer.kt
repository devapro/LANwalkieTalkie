package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class SocketServer(private val connectionListener: IServer.ConnectionListener) : IServer {
    companion object {
        const val SERVER_PORT = 9700
    }

    private val executorService = Executors.newCachedThreadPool()
    private val pingExecutor = Executors.newScheduledThreadPool(1)

    /**
     * Data for sending
     */
    private val outputQueueMap = HashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    override fun initServer(): Int {
        val socket = ServerSocket(SERVER_PORT)
        executorService.execute {
            while (true) {
                try {
                    val client = socket.accept()
                    client.sendBufferSize = 8192
                    connectionListener.onClientConnected(
                        InetSocketAddress(
                            client.inetAddress.hostAddress,
                            client.port
                        )
                    )
                    outputQueueMap[client.inetAddress.hostAddress] = LinkedBlockingDeque()
                    executorService.execute {
                        val outputStream = DataOutputStream(client.getOutputStream())
                        try {
                            var errorCounter = 0
                            while (client.isConnected) {
                                try {
                                    if (outputQueueMap[client.inetAddress.hostAddress]?.isNotEmpty() == true) {
                                        val buf =
                                            outputQueueMap[client.inetAddress.hostAddress]?.pollFirst()
                                        buf?.let { outputStream.write(it.array()) }
                                        errorCounter = 0
                                    }
                                } catch (e: Exception) {
                                    errorCounter++
                                    if (errorCounter > 3) {
                                        throw e
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.w(e)
                        } finally {
                            connectionListener.onClientDisconnected(
                                InetSocketAddress(
                                    client.inetAddress.hostAddress,
                                    client.port
                                )
                            )
                            outputQueueMap.remove(client.inetAddress.hostAddress)
                        }
                    }

                    executorService.execute {
                        val dataInput = DataInputStream(client.getInputStream())
                        val byteArray = ByteArray(8192 * 2)
                        try {
                            while (client.isConnected && !client.isInputShutdown) {
                                val readCount = dataInput.read(byteArray)
                                if (readCount > 0) {
                                    read(byteArray, readCount, client.inetAddress.hostAddress)
                                }
                                java.util.Arrays.fill(byteArray, 0)
                            }
                        } catch (e: Exception) {
                            Timber.w(e)
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
        }
        pingExecutor.scheduleWithFixedDelay({ ping() }, 1000, 5000, TimeUnit.MILLISECONDS)
        return SERVER_PORT
    }

    private fun ping() {
        outputQueueMap.forEach { item ->
            item.value.add(ByteBuffer.wrap("ping".toByteArray()))
        }
    }

    override fun stop() {
        executorService.shutdown()
        pingExecutor.shutdown()
    }

    override fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        val message = String(rspData).trim()
        Timber.i("message: $message from $hostAddress")
    }
}