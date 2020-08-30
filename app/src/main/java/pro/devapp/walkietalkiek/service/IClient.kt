package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdServiceInfo
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

interface IClient {
    fun addClient(socketAddress: InetSocketAddress)
    fun removeClient(nsdServiceInfo: NsdServiceInfo)
    fun stop()

    data class Connection(val socketChannel: SocketChannel, var isPendingRemove: Boolean)
}