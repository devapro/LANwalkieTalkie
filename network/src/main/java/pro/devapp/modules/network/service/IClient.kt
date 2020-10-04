package pro.devapp.modules.network.service

import java.net.InetSocketAddress

interface IClient {
    fun addClient(socketAddress: InetSocketAddress, ignoreExist: Boolean = true)
    fun removeClient(hostAddress: String)
    fun stop()
}