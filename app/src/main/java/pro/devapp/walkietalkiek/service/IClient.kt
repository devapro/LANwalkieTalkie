package pro.devapp.walkietalkiek.service

import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

interface IClient {
    fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean = true)
    fun removeClient(hostAddress: String)
    fun stop()

    data class Connection(val socketChannel: SocketChannel, var isPendingRemove: Boolean)
}