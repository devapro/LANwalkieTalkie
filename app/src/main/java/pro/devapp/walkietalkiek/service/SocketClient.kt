package pro.devapp.walkietalkiek.service

import timber.log.Timber
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SocketClient(private val receiverListener: (bytes: ByteArray) -> Unit) : IClient {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceClients = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = HashMap<String, Connection>()
    private val lockRead = Object()
    private val lock = Object()

    var actionListener: ActionListener? = null

    override fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean) {
        if (sockets[socketAddress.address.hostAddress] == null || ignoreExist) {
            executorService.execute {
                synchronized(lock) {
                    if (sockets[socketAddress.address.hostAddress] == null || ignoreExist) {
                        try {
                            sockets[socketAddress.address.hostAddress]?.apply {
                                future?.cancel(true)
                                socket.close()
                                sockets.remove(socketAddress.address.hostAddress)
                            }
                            val socket =
                                Socket(socketAddress.address.hostAddress, socketAddress.port)
                            sockets[socketAddress.address.hostAddress] = Connection(socket, null)
                            actionListener?.onClientListUpdated(sockets.map { it.key }.toList())
                            handleConnection(socketAddress.address.hostAddress)
                        } catch (e: Exception) {
                            Timber.w(e)
                            Timber.i("connection error ${socketAddress.address.hostAddress}")
                        }
                    }
                }
            }
        }
    }

    override fun removeClient(hostAddress: String) {
        sockets[hostAddress]?.apply {
            val socketAddress = InetSocketAddress(
                hostAddress,
                socket.port
            )
            future?.cancel(true)
            socket.close()
            sockets.remove(hostAddress)
            Timber.i("removeClient $hostAddress")
            actionListener?.onClientListUpdated(sockets.map { it.key }.toList())
            addClient(socketAddress)
        }
    }

    override fun stop() {
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun handleConnection(hostAddress: String) {
        val socket = sockets[hostAddress]
        socket?.let {
            executorServiceClients.execute {
                if (!it.socket.isClosed) {
                    val dataInput = DataInputStream(it.socket.getInputStream())
                    val byteArray = ByteArray(8192 * 2)
                    Timber.i("Started reading $hostAddress")
                    try {
                        while (!it.socket.isClosed && !it.socket.isInputShutdown) {
                            val readCount = dataInput.read(byteArray)
                            if (readCount > 0) {
                                read(byteArray, readCount, hostAddress)
                            }
                            java.util.Arrays.fill(byteArray, 0)
                        }
                    } catch (e: Exception) {
                        Timber.w(e)
                    } finally {
                        removeClient(hostAddress)
                        Timber.i("remove $hostAddress")
                    }
                } else {
                    removeClient(hostAddress)
                    Timber.i("remove $hostAddress")
                }
            }
        }
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        if (readCount > 20) {
            executorServiceReader.submit {
                receiverListener(rspData)
            }
            executorServiceReader.execute { actionListener?.onClientSendMessage(hostAddress) }
            Timber.i("message: audio $readCount")
        } else {
            val message = String(rspData).trim()
            Timber.i("message: $message from $hostAddress")
        }
    }

    interface ActionListener {
        fun onClientListUpdated(clients: List<String>)
        fun onClientSendMessage(client: String)
    }

    data class Connection(
        val socket: Socket,
        val future: Future<*>?
    )
}