package pro.devapp.walkietalkiek.service

import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.*

class SocketClient : IClient {
    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceClients = Executors.newCachedThreadPool()
    private val executorServiceReader = Executors.newFixedThreadPool(1)
    private val sockets = ConcurrentHashMap<String, Connection>()
    private val lock = Object()

    val clientConnectionSubject = PublishSubject.create<String>()
    val clientDisconnectionSubject = PublishSubject.create<String>()

    /**
     * Data for sending
     */
    private val outputQueueMap = ConcurrentHashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }

    override fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean) {
        val hostAddress = socketAddress.address.hostAddress
        if ((sockets[hostAddress] == null || ignoreExist) && !executorService.isShutdown) {
            executorService.execute {
                synchronized(lock) {
                    if (sockets[hostAddress] == null || ignoreExist) {
                        try {
                            sockets[hostAddress]?.apply {
                                future?.cancel(true)
                                socket.close()
                                sockets.remove(hostAddress)
                            }
                            val socket =
                                Socket(hostAddress, socketAddress.port)
                            socket.receiveBufferSize = 8192 * 2
                            sockets[hostAddress] = Connection(socket, null)
                            outputQueueMap[hostAddress] = LinkedBlockingDeque()
                            clientConnectionSubject.onNext(hostAddress)
                            handleConnection(hostAddress)
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
            clientDisconnectionSubject.onNext(hostAddress)
            // try reconnect
            addClient(socketAddress)
        }
    }

    override fun stop() {
        sockets.forEach {
            it.value.future?.cancel(true)
            it.value.socket.close()
        }
        executorService.shutdown()
        executorServiceReader.shutdown()
        executorServiceClients.shutdown()
    }

    private fun handleConnection(hostAddress: String) {
        val socket = sockets[hostAddress]
        socket?.let {
            val readingFuture = executorServiceClients.submit {
                val dataInput = DataInputStream(it.socket.getInputStream())
                val byteArray = ByteArray(8192 * 8)
                Timber.i("Started reading $hostAddress")
                try {
                    while (!it.socket.isClosed && !it.socket.isInputShutdown) {
                        val readCount = dataInput.read(byteArray)
                        if (readCount > 0) {

                        }
                        Arrays.fill(byteArray, 0)
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                } finally {

                }
            }
            executorServiceClients.execute {
                if (!it.socket.isClosed) {
                    try {
                        val outputStream = DataOutputStream(it.socket.getOutputStream())
                        var errorCounter = 0
                        while (it.socket.isConnected && !it.socket.isClosed) {
                            try {
                                val buf =
                                    if (outputQueueMap[it.socket.inetAddress.hostAddress]?.isEmpty() == true) {
                                        outputQueueMap[it.socket.inetAddress.hostAddress]?.pollFirst(
                                            1000,
                                            TimeUnit.MILLISECONDS
                                        )
                                    } else {
                                        outputQueueMap[it.socket.inetAddress.hostAddress]?.pollFirst()
                                    }
                                buf?.let { byteArray ->
                                    outputStream.write(byteArray.array())
                                    outputStream.flush()
                                    Timber.i("send data to ${it.socket.inetAddress.hostAddress}")
                                }
                                errorCounter = 0
                            } catch (e: Exception) {
                                errorCounter++
                                if (errorCounter > 3) {
                                    Timber.d("errorCounter $errorCounter")
                                    throw e
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Timber.w(e)
                    } finally {
                        readingFuture.cancel(true)
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

    data class Connection(
        val socket: Socket,
        val future: Future<*>?
    )
}