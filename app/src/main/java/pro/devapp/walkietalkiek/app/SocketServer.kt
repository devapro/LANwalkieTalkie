package pro.devapp.walkietalkiek.app

import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class SocketServer(
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val clientSocket: SocketClient
) : IServer {
    companion object {
        const val SERVER_PORT = 9700
    }

    private val voicePlayer = VoicePlayer()

    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceRead = Executors.newCachedThreadPool()
    private val acceptConnectionExecutor = Executors.newScheduledThreadPool(1)

    /**
     * Data for sending
     */
    private val outputQueueMap = ConcurrentHashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    private var socket: ServerSocket? = null

    override fun initServer(): Int {
        if (socket != null && socket?.isClosed == false) {
            return SERVER_PORT
        }
        socket = ServerSocket(SERVER_PORT)
        socket?.let {
            acceptConnectionExecutor.scheduleWithFixedDelay({
                try {
                    it.reuseAddress = true
                    val client = it.accept()
                    client.sendBufferSize = 8192
                    client.receiveBufferSize = 8192 * 2
                    client.tcpNoDelay = true
                    val hostAddress = client.inetAddress.hostAddress
                    outputQueueMap[hostAddress] = LinkedBlockingDeque()
                    clientSocket.addClient(InetSocketAddress(
                        hostAddress,
                        client.port
                    ), false)
                    connectedDevicesRepository.addOrUpdateHostStateToConnected(hostAddress)
                    handleConnection(client)
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
        voicePlayer.create()
        voicePlayer.startPlay()
        return SERVER_PORT
    }

    private fun handleConnection(client: Socket) {
        val hostAddress = client.inetAddress.hostAddress
        var errorCounter = 0
        val readingFuture = executorService.submit {
            val dataInput = DataInputStream(client.getInputStream())
            val byteArray = ByteArray(8192 * 8)
            Timber.i("Started reading $hostAddress")
            try {
                while (!client.isClosed && !client.isInputShutdown) {
                    val readCount = dataInput.read(byteArray)
                    if (readCount > 0) {
                        read(byteArray, readCount, hostAddress)
                    }
                    Arrays.fill(byteArray, 0)
                }
            } catch (e: Exception) {
                Timber.w(e)
            } finally {
                closeClient(client)
            }
        }
        executorService.submit {
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
                            Timber.i("send data to $hostAddress")
                        }
                        errorCounter = 0
                    } catch (e: Exception) {
                        errorCounter++
                        if (errorCounter > 3) {
                            Timber.d("errorCounter $errorCounter")
                            readingFuture.cancel(true)
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
            voicePlayer.play(data)
            Timber.i("message: audio ${data.size} from $hostAddress")
        } else {
            val message = String(data).trim()
            Timber.i("message: $message from $hostAddress")
        }
        connectedDevicesRepository.storeDataReceivedTime(hostAddress)
//        executorServiceRead.submit {
//            receiverListener(hostAddress, rspData)
//        }

//        if (readCount > 20) {
//            executorServiceRead.submit {
//                receiverListener(rspData)
//            }
//            Timber.i("message: audio $readCount")
//        } else {
//            val message = String(rspData).trim()
//            Timber.i("message: $message from $hostAddress")
////            if (message == "ping"){
////                sockets[hostAddress]?.apply {
////                    try {
////                        socket.getOutputStream().write("pong".toByteArray())
////                    } catch (e: Exception){
////                        removeClient(hostAddress)
////                    }
////                }
////            }
//        }
    }

    override fun stop() {
        socket?.apply {
            close()
        }
        voicePlayer.stopPlay()
        executorService.shutdown()
        acceptConnectionExecutor.shutdown()
        executorServiceRead.shutdown()
    }

    override fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueueMap.forEach { item ->
            item.value.add(byteBuffer)
        }
    }
}