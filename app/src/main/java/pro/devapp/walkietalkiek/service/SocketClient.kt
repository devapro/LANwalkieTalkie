package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class SocketClient(private val receiverListener: (bytes: ByteArray) -> Unit) : IClient {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, Socket>()
    private val lockRead = Object()
    private val lock = Object()

    override fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean) {
        executorService.execute {
            // check connection
//            if (sockets[socketAddress.address.hostAddress] != null) {
//               try {
//                   Timber.i("connection exist ${socketAddress.address.hostAddress}")
//                   checkConnection(socketAddress.address.hostAddress)
//                   Timber.i("connection tested ${socketAddress.address.hostAddress}")
//               } catch (e: Exception) {
//                   sockets.remove(socketAddress.address.hostAddress)
//                   Timber.i("connection was, but remove after test  ${socketAddress.address.hostAddress}")
//               }
//            }
//            sockets[socketAddress.address.hostAddress]?.apply {
//                close()
//                sockets.remove(socketAddress.address.hostAddress)
//            }
            //
            if (sockets[socketAddress.address.hostAddress] == null || ignoreExist) {
                try {
                    val socket = Socket(socketAddress.address.hostAddress, socketAddress.port)
                    sockets[socketAddress.address.hostAddress] = socket
                    val dataInput = DataInputStream(socket.getInputStream())
                    val byteArray = ByteArray(8192 * 2)
                    Timber.i("Started reading ${socketAddress.address.hostAddress}")
                    try {
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
                } catch (e: Exception) {
                    Timber.w(e)
                    Timber.i("connection error ${socketAddress.address.hostAddress}")
                }
            }
        }
    }

    override fun removeClient(hostAddress: String) {
        sockets[hostAddress]?.apply {
            val socketAddress = InetSocketAddress(
                hostAddress,
                port
            )
            close()
            sockets.remove(hostAddress)
            Timber.i("removeClient $hostAddress")
            addClient(socketAddress)
        }
    }

    override fun stop() {
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun checkConnection(hostAddress: String) {
        val outputStream = DataOutputStream(sockets[hostAddress]!!.getOutputStream())
        outputStream.write("pong".toByteArray())
    }

    private fun handleConnection(hostAddress: String) {
        val socket = sockets[hostAddress]
        socket?.let {
            val dataInput = DataInputStream(it.getInputStream())
            val byteArray = ByteArray(8192 * 8)
            Timber.i("Start reading $hostAddress")
            while (it.isConnected && !it.isInputShutdown) {
                val readCount = dataInput.read(byteArray)
                if (readCount > 0) {
                    read(byteArray, readCount, hostAddress)
                }
                java.util.Arrays.fill(byteArray, 0)
            }
        }
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        if (readCount > 20) {
            synchronized(lockRead) {
                receiverListener(rspData)
            }
            Timber.i("message: audio $readCount")
        } else {
            val message = String(rspData).trim()
            Timber.i("message: $message from $hostAddress")
            if (message == "ping") {
                checkConnection(hostAddress)
            }
        }
    }
}