package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdServiceInfo
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
                    val buffer = ByteArray(8192 * 8)
                    while (socket.isConnected) {
                        val readCount = dataInput.read(buffer)
                        if (readCount > 0) {
                            read(buffer, readCount, socketAddress.address.hostAddress)
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e)
                }
                sockets.remove(socketAddress.address.hostAddress)
            }
        }
    }

    override fun removeClient(nsdServiceInfo: NsdServiceInfo) {
        //  sockets[nsdServiceInfo.host.hostAddress]?.apply { close() }
    }

    override fun stop() {
        executorService.shutdown()
        executorServiceReader.shutdown()
    }

    private fun read(byteArray: ByteArray, readCount: Int, serviceName: String) {
        if (readCount > 20) {
            synchronized(lockRead) {
                receiverListener(byteArray)
            }
            Timber.i("message: audio $readCount")
        } else {
            val rspData = ByteArray(readCount)
            System.arraycopy(byteArray, 0, rspData, 0, readCount)
            Timber.i("message: ${String(rspData).trim()} from $serviceName")
        }
    }
}