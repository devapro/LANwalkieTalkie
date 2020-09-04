package pro.devapp.walkietalkiek.service

import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

interface IClient {
    fun addClient(socketAddress: InetSocketAddress)
    fun removeClient(hostAddress: String)
    fun stop()

    data class Connection(val socketChannel: SocketChannel, var isPendingRemove: Boolean)
}