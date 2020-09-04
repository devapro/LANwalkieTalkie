package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class SocketClient(private val receiverListener: (bytes: ByteArray) -> Unit) : IClient {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, Socket>()
    private val lockRead = Object()

    override fun addClient(socketAddress: InetSocketAddress) {
        executorService.execute {
            if (sockets[socketAddress.address.hostAddress] == null) {
                try {
                    val socket = Socket(socketAddress.address.hostAddress, socketAddress.port)
                    sockets[socketAddress.address.hostAddress] = socket
                    val dataInput = DataInputStream(socket.getInputStream())
                    val byteArray = ByteArray(8192 * 8)
                    Timber.i("Start reading ${socketAddress.address.hostAddress}")
                    while (socket.isConnected && !socket.isInputShutdown) {
                        val readCount = dataInput.read(byteArray)
                        if (readCount > 0) {
                            read(byteArray, readCount, socketAddress.address.hostAddress)
                        }
                        java.util.Arrays.fill(byteArray, 0)
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                }
                sockets.remove(socketAddress.address.hostAddress)
                Timber.i("remove ${socketAddress.address.hostAddress}")
            }
        }
    }

    override fun removeClient(hostAddress: String) {
        sockets[hostAddress]?.apply {
            if (isInputShutdown || !isConnected) {
                Timber.i("close: $hostAddress")
                close()
            } else {
                Timber.i("try close, but it is connected : $hostAddress")
            }
        }
    }

    override fun stop() {
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int, serviceName: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        if (readCount > 20) {
            synchronized(lockRead) {
                receiverListener(rspData)
            }
            Timber.i("message: audio $readCount")
        } else {
            Timber.i("message: ${String(rspData).trim()} from $serviceName")
        }
    }
}