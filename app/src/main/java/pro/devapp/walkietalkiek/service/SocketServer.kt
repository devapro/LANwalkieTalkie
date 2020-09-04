package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

class SocketServer(private val connectionListener: IServer.ConnectionListener) : IServer {
    companion object {
        const val SERVER_PORT = 9700
    }

    private val executorService = Executors.newCachedThreadPool()

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
                    connectionListener.onNewClient(
                        InetSocketAddress(
                            client.inetAddress.hostAddress,
                            client.port
                        )
                    )
                    outputQueueMap[client.inetAddress.hostAddress] = LinkedBlockingDeque()
                    executorService.execute {
                        val outputStream = DataOutputStream(client.getOutputStream())
                        try {
                            while (client.isConnected) {
                                if (outputQueueMap[client.inetAddress.hostAddress]?.isNotEmpty() == true) {
                                    val buf =
                                        outputQueueMap[client.inetAddress.hostAddress]?.pollFirst()
                                    buf?.let { outputStream.write(it.array()) }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.w(e)
                        } finally {
                            outputQueueMap.remove(client.inetAddress.hostAddress)
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
        }
        return SERVER_PORT
    }

    override fun stop() {
        executorService.shutdown()
    }

    override fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }
}