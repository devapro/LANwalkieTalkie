package pro.devapp.walkietalkiek.service

import java.net.InetSocketAddress
import java.nio.ByteBuffer

interface IServer {
    fun initServer(): Int
    fun stop()
    fun sendMessage(byteBuffer: ByteBuffer)
    interface ConnectionListener {
        fun onClientConnected(address: InetSocketAddress)
        fun onClientDisconnected(address: InetSocketAddress)
    }
}