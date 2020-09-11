package pro.devapp.walkietalkiek.service

import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SocketServer(
    private val receiverListener: (hostAddress: String, bytes: ByteArray) -> Unit
) : IServer {
    companion object {
        const val SERVER_PORT = 9700
    }

    private val executorService = Executors.newCachedThreadPool()
    private val executorServiceRead = Executors.newCachedThreadPool()
    private val pingExecutor = Executors.newScheduledThreadPool(1)
    private val acceptConnectionExecutor = Executors.newScheduledThreadPool(1)

//    /**
//     * Data for sending
//     */
//    private val outputQueueMap = HashMap<String, LinkedBlockingDeque<ByteBuffer>>()

    private var socket: ServerSocket? = null

    val clientConnectionSubject = PublishSubject.create<InetSocketAddress>()
    val clientDisconnectionSubject = PublishSubject.create<InetSocketAddress>()

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
                    clientConnectionSubject.onNext(
                        InetSocketAddress(
                            hostAddress,
                            client.port
                        )
                    )
                    //  outputQueueMap[hostAddress] = LinkedBlockingDeque()
                    handleConnection(client)
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }, 100, 1000, TimeUnit.MILLISECONDS)
        }
        // pingExecutor.scheduleWithFixedDelay({ ping() }, 1000, 5000, TimeUnit.MILLISECONDS)
        return SERVER_PORT
    }

    private fun handleConnection(client: Socket) {
        executorService.submit {
            val hostAddress = client.inetAddress.hostAddress
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
                client.close()
                clientDisconnectionSubject.onNext(
                    InetSocketAddress(
                        hostAddress,
                        client.port
                    )
                )
                // outputQueueMap.remove(hostAddress)
            }
        }
    }

    private fun read(byteArray: ByteArray, readCount: Int, hostAddress: String) {
        val rspData = ByteArray(readCount)
        System.arraycopy(byteArray, 0, rspData, 0, readCount)
        executorServiceRead.submit {
            receiverListener(hostAddress, rspData)
        }

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

//    private fun ping() {
//        outputQueueMap.forEach { item ->
//            if (item.value.isEmpty()) {
//                item.value.add(ByteBuffer.wrap("ping".toByteArray()))
//            }
//        }
//    }

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
//        outputQueueMap.forEach { item ->
//            item.value.add(byteBuffer)
//        }
    }
}