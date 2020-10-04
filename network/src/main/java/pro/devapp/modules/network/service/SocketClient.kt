package pro.devapp.modules.network.service

import io.reactivex.schedulers.Schedulers
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
    private val reconnectTimer = Executors.newSingleThreadScheduledExecutor()
    private val sockets = ConcurrentHashMap<String, Connection>()
    private val lock = Object()

    val clientConnectionSubject =
        PublishSubject.create<String>().apply { subscribeOn(Schedulers.io()) }
    val clientDisconnectionSubject =
        PublishSubject.create<String>().apply { subscribeOn(Schedulers.io()) }

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
            reconnectTimer.schedule({ addClient(socketAddress) }, 1000, TimeUnit.MILLISECONDS)
        }
    }

    override fun stop() {
        sockets.forEach {
            it.value.future?.cancel(true)
            it.value.socket.close()
        }
        reconnectTimer.shutdown()
        executorService.shutdown()
        executorServiceReader.shutdown()
        executorServiceClients.shutdown()
    }

    private fun handleConnection(hostAddress: String) {
        val socket = sockets[hostAddress]?.socket
        socket?.let {
            val readingFuture = executorServiceClients.submit {
                val dataInput = DataInputStream(socket.getInputStream())
                val byteArray = ByteArray(8192 * 8)
                Timber.i("Started reading $hostAddress")
                try {
                    while (!socket.isClosed && !socket.isInputShutdown) {
                        val readCount = dataInput.read(byteArray)
                        if (readCount > 0) {

                        }
                        Arrays.fill(byteArray, 0)
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                } finally {
                    sockets[hostAddress]?.future?.cancel(true)
                    removeClient(hostAddress)
                    Timber.i("remove 3 $hostAddress")
                }
            }
            executorServiceClients.submit {
                if (!socket.isClosed) {
                    try {
                        val outputStream = DataOutputStream(socket.getOutputStream())
                        var errorCounter = 0
                        while (socket.isConnected && !socket.isClosed) {
                            try {
                                val buf =
                                    if (outputQueueMap[socket.inetAddress.hostAddress]?.isEmpty() == true) {
                                        outputQueueMap[socket.inetAddress.hostAddress]?.pollFirst(
                                            1000,
                                            TimeUnit.MILLISECONDS
                                        )
                                    } else {
                                        outputQueueMap[socket.inetAddress.hostAddress]?.pollFirst()
                                    }
                                buf?.let { byteArray ->
                                    outputStream.write(byteArray.array())
                                    outputStream.flush()
                                    Timber.i("send data to ${socket.inetAddress.hostAddress}")
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
                        Timber.i("remove 1 $hostAddress")
                    }
                } else {
                    removeClient(hostAddress)
                    Timber.i("remove 2 $hostAddress")
                }
            }.also {
                sockets[hostAddress] = Connection(socket, it)
            }
        }
    }

    data class Connection(
        val socket: Socket,
        val future: Future<*>?
    )
}