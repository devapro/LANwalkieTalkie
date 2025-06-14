package pro.devapp.walkietalkiek.serivce.network

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.TextMessagesRepository
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SocketServer(
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val clientSocket: SocketClient,
    private val textMessagesRepository: TextMessagesRepository,
    private val coroutineContextProvider: CoroutineContextProvider
) {

    private val acceptConnectionScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val readDataScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val writeDataScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io
    )

    private val SERVER_PORT by lazy {
        getPort().also { port ->
            Timber.Forest.i("Server port initialized: $port")
        }
    }

    private fun getPort(): Int {
        return Random.nextInt(1111, 9999).also { port ->
            Timber.Forest.i("Generated random port: $port")
        }
    }

    /**
     * Data for sending
     */
    private val outputQueueMap = ConcurrentHashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    private var socket: ServerSocket? = null

    var dataListener: ((bytes: ByteArray) -> Unit)? = null

    fun initServer(): Int {
        if (socket != null && socket?.isClosed == false) {
            return SERVER_PORT
        }
        socket = ServerSocket(SERVER_PORT).apply {
            reuseAddress = false
            soTimeout = 5000 // Set a timeout for accept to avoid blocking indefinitely
        }
        acceptConnectionScope.launch {
            delay(100L)
            while (acceptConnectionScope.isActive) {
                try {
                    val client = socket!!.accept()
                    client.sendBufferSize = 8192
                    client.receiveBufferSize = 8192 * 2
                    client.tcpNoDelay = true
                    val hostAddress = client.inetAddress.hostAddress.orEmpty()
                    outputQueueMap[hostAddress] = LinkedBlockingDeque()
                    connectedDevicesRepository.addOrUpdateHostStateToConnected(hostAddress, client.port)
                    handleConnection(client)
                } catch (e: Exception) {
                    Timber.Forest.w(e)
                }
                delay(1000L)
            }
        }
        return SERVER_PORT
    }

    private fun handleConnection(client: Socket) {
        val hostAddress = client.inetAddress.hostAddress
        var errorCounter = 0
        val readingFuture = readDataScope.launch {
            val dataInput = DataInputStream(client.getInputStream())
            val byteArray = ByteArray(8192 * 8)
            Timber.Forest.i("Started reading $hostAddress")
            try {
                while (!client.isClosed && !client.isInputShutdown) {
                    val readCount = dataInput.read(byteArray)
                    if (readCount > 0) {
                        read(byteArray, readCount, hostAddress)
                    }
                    Arrays.fill(byteArray, 0)
                }
            } catch (e: Exception) {
                Timber.Forest.w(e)
            } finally {
                closeClient(client)
            }
        }
        writeDataScope.launch {
            if (!client.isClosed) {
                val outputStream = DataOutputStream(client.getOutputStream())
                while (!client.isClosed && !client.isOutputShutdown) {
                    try {
                        val buf =
                            if (outputQueueMap[hostAddress]?.isEmpty() == true) {
                                outputQueueMap[hostAddress]?.pollFirst(
                                    5000,
                                    TimeUnit.MILLISECONDS
                                )
                            } else {
                                outputQueueMap[hostAddress]?.pollFirst()
                            }
                        buf?.let { byteArray ->
                            outputStream.write(byteArray.array())
                            outputStream.flush()
                            Timber.Forest.i("send data to $hostAddress")
                        }
                        errorCounter = 0
                    } catch (e: Exception) {
                        errorCounter++
                        if (errorCounter > 3) {
                            Timber.Forest.d("errorCounter $errorCounter")
                            readingFuture.cancel()
                            closeClient(client)
                        }
                    }
                }
            }
        }
    }

    private fun closeClient(client: Socket) {
        val hostAddress = client.inetAddress.hostAddress
        client.close()
        outputQueueMap.remove(hostAddress)
        clientSocket.removeClient(hostAddress)
        connectedDevicesRepository.setHostDisconnected(hostAddress)
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val data = ByteArray(readCount)
        System.arraycopy(byteArray, 0, data, 0, readCount)
        if (data.size > 20) {
            dataListener?.invoke(data)
            Timber.Forest.i("message: audio ${data.size} from $hostAddress")
        } else {
            val message = String(data).trim()
            Timber.Forest.i("message: $message from $hostAddress")
            if (message == "ping"){
                clientSocket.sendMessageToHost(
                    hostAddress = hostAddress,
                    byteBuffer = ByteBuffer.wrap("pong".toByteArray())
                )
            } else {
                textMessagesRepository.addMessage(
                    message = message,
                    hostAddress = hostAddress
                )
            }
        }
        connectedDevicesRepository.storeDataReceivedTime(hostAddress)
    }

    fun stop() {
        socket?.apply {
            close()
        }
        readDataScope.cancel()
        writeDataScope.cancel()
        acceptConnectionScope.cancel()
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }
}