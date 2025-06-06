package pro.devapp.walkietalkiek.serivce.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class SocketClient (
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    private val sockets = ConcurrentHashMap<String, Socket>()

    private val lock = Mutex()

    private val reconnectTimerScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val addClientScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io.limitedParallelism(1)
    )

    private val clientsScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val readDataScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val writeDataScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    /**
     * Data for sending
     */
    private val outputQueueMap = ConcurrentHashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }

    fun sendMessageToHost(hostAddress: String, byteBuffer: ByteBuffer) {
        outputQueueMap[hostAddress]?.add(byteBuffer)
    }

    fun addClient(socketAddress: InetSocketAddress) {
        socketAddress.address.hostAddress?.let { hostAddress ->
            addClientScope.launch {
                lock.withLock {
                    try {
                        sockets[hostAddress]?.apply {
                            close()
                            sockets.remove(hostAddress)
                        }
                        while (sockets[hostAddress]?.isClosed?.not() == true) {
                            Timber.Forest.i("Waiting for socket to close $hostAddress")
                            delay(100L)
                        }
                        val socket = Socket(hostAddress, socketAddress.port)
                        socket.receiveBufferSize = 8192 * 2
                        sockets[hostAddress] = socket
                        outputQueueMap[hostAddress] = LinkedBlockingDeque()
                        Timber.Forest.i("AddClient $hostAddress ${socketAddress.port}")
                        connectedDevicesRepository.addOrUpdateHostStateToConnected(hostAddress)
                        handleConnection(socket)
                    } catch (e: Exception) {
                        Timber.Forest.w(e)
                        Timber.Forest.i("connection error ${hostAddress} ${socketAddress.port}")
                        connectedDevicesRepository.setHostDisconnected(hostAddress)
                        // try reconnect
                        reconnectTimerScope.launch {
                            Timber.Forest.i("reconnect $hostAddress")
                            delay(1000L)
                            addClient(socketAddress)
                        }
                    }
                }
            }
        }
    }

    fun removeClient(hostAddress: String) {
        Timber.Forest.i("removeClient $hostAddress")
        sockets[hostAddress]?.apply {
            val socketAddress = InetSocketAddress(
                hostAddress,
                port
            )
            close()
            sockets.remove(hostAddress)
            Timber.Forest.i("removeClient $hostAddress $port")
            connectedDevicesRepository.setHostDisconnected(hostAddress)
            // try reconnect
            reconnectTimerScope.launch {
                delay(1000L)
                Timber.Forest.i("reconnect $hostAddress $port")
                addClient(socketAddress)
            }
        }
    }

    fun stop() {
        sockets.forEach {
            it.value.close()
        }
        reconnectTimerScope.cancel()
        addClientScope.cancel()
        clientsScope.cancel()
        readDataScope.cancel()
        writeDataScope.cancel()
    }

    private fun handleConnection(socket: Socket) {
        val readingFuture = readDataScope.launch {
            try {
                val dataInput = DataInputStream(socket.getInputStream())
                val byteArray = ByteArray(8192 * 8)
                Timber.Forest.i("Started reading ${socket.inetAddress.hostAddress}")
                while (!socket.isClosed && !socket.isInputShutdown) {
                    val readCount = dataInput.read(byteArray)
                    if (readCount > 0) {

                    }
                    Arrays.fill(byteArray, 0)
                }
            } catch (e: Exception) {
                Timber.Forest.w(e)
                removeClient(socket.inetAddress.hostAddress)
            } finally {

            }
        }
        writeDataScope.launch {
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
                            Timber.Forest.i("send data to ${socket.inetAddress.hostAddress}")
                        }
                        errorCounter = 0
                    } catch (e: Exception) {
                        errorCounter++
                        if (errorCounter > 3) {
                            Timber.Forest.d("errorCounter $errorCounter")
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.Forest.w(e)
            } finally {
                readingFuture.cancel()
                removeClient(socket.inetAddress.hostAddress)
                Timber.Forest.i("remove ${socket.inetAddress.hostAddress}")
            }
        }
    }
}