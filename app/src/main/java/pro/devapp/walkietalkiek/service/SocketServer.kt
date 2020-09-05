package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
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
    private val executorServiceRead = Executors.newCachedThreadPool()
    private val pingExecutor = Executors.newScheduledThreadPool(1)
    private val acceptConnectionExecutor = Executors.newScheduledThreadPool(1)

    /**
     * Data for sending
     */
    private val outputQueueMap = HashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    private var socket: ServerSocket? = null

    override fun initServer(): Int {
        socket = ServerSocket(SERVER_PORT)
        socket?.let {
            acceptConnectionExecutor.scheduleWithFixedDelay({
                try {
                    it.reuseAddress = true
                    val client = it.accept()
                    client.sendBufferSize = 8192
                    client.tcpNoDelay = true
                    connectionListener.onClientConnected(
                        InetSocketAddress(
                            client.inetAddress.hostAddress,
                            client.port
                        )
                    )
                    outputQueueMap[client.inetAddress.hostAddress] = LinkedBlockingDeque()
                    handleConnection(client)
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
        pingExecutor.scheduleWithFixedDelay({ ping() }, 1000, 5000, TimeUnit.MILLISECONDS)
        return SERVER_PORT
    }

    private fun handleConnection(client: Socket) {
        executorService.submit {
            val outputStream = DataOutputStream(client.getOutputStream())
            try {
                var errorCounter = 0
                while (client.isConnected) {
                    try {
                        val buf =
                            if (outputQueueMap[client.inetAddress.hostAddress]?.isEmpty() == true) {
                                outputQueueMap[client.inetAddress.hostAddress]?.pollFirst(
                                    1000,
                                    TimeUnit.MILLISECONDS
                                )
                            } else {
                                outputQueueMap[client.inetAddress.hostAddress]?.pollFirst()
                            }
                        buf?.let {
                            outputStream.write(it.array())
                            outputStream.flush()
                            Timber.i("send data to ${client.inetAddress.hostAddress}")
                        }
                        errorCounter = 0
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
                client.close()
                connectionListener.onClientDisconnected(
                    InetSocketAddress(
                        client.inetAddress.hostAddress,
                        client.port
                    )
                )
                outputQueueMap.remove(client.inetAddress.hostAddress)
            }
        }
//        executorServiceRead.submit {
//            val dataInput = DataInputStream(client.getInputStream())
//            val byteArray = ByteArray(8192 * 8)
//            Timber.i("Started reading ${client.inetAddress.hostAddress}")
//            try {
//                while (!client.isClosed && !client.isInputShutdown) {
//                    val readCount = dataInput.read(byteArray)
//                    if (readCount > 0) {
//                        read(byteArray, readCount, client.inetAddress.hostAddress)
//                    }
//                    java.util.Arrays.fill(byteArray, 0)
//                }
//            } catch (e: Exception) {
//                Timber.w(e)
//            }
//        }
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        val message = String(rspData).trim()
        Timber.i("message: $message from $hostAddress")
    }

    private fun ping() {
        outputQueueMap.forEach { item ->
            if (item.value.isEmpty()) {
                item.value.add(ByteBuffer.wrap("ping".toByteArray()))
            }
        }
    }

    override fun stop() {
        socket?.apply {
            close()
        }
        executorService.shutdown()
        pingExecutor.shutdown()
        acceptConnectionExecutor.shutdown()
        executorServiceRead.shutdown()
    }

    override fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }
}