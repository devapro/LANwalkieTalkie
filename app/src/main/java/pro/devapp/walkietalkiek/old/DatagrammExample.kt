package pro.devapp.walkietalkiek.old

import android.os.Build
import android.util.Log
import java.lang.String
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.Executors

private fun handleConnection(addr: InetSocketAddress) {
    // https://stackoverflow.com/questions/31337387/datagramchannel-not-receiving-any-bytes-on-android
    // https://stackoverflow.com/questions/14690226/how-to-use-selector-object-with-datagramchannel-to-do-non-blocking-packet-recept
    val selector = Selector.open()
    val datagramChannel = DatagramChannel.open()
    val socket = datagramChannel.socket()
    datagramChannel.configureBlocking(false)
    socket.reuseAddress = true
    datagramChannel.register(selector, SelectionKey.OP_WRITE)
    datagramChannel.connect(addr)
    val executor = Executors.newCachedThreadPool()
    executor.execute() {
        try {
            while (selector.isOpen) {
                selector.select()
                val keys =
                    selector.selectedKeys().iterator()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.isReadable) {
                        handleRead(key)
                    }
                    if (key.isValid && key.isWritable) {
                        handleWrite(key, addr)
                    }
                    keys.remove()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!selector.isOpen) {
            executor.shutdown()
        }
    }
}


private fun handleWrite(key: SelectionKey, addr: InetSocketAddress) {
    val channel =
        key.channel() as DatagramChannel

    byteBuffer.clear()
    byteBuffer.putInt(1234)
    byteBuffer.flip()

    val bytes = channel.send(byteBuffer, addr)

    Log.i(ResolveListener.LOG_TAG, kotlin.String.format("Send %d bytes to %s", bytes, addr))

    key.interestOps(SelectionKey.OP_READ)
}

private val byteBuffer: ByteBuffer = ByteBuffer.allocate(256)
private var from: SocketAddress? = null
private fun handleRead(key: SelectionKey) {
    val channel =
        key.channel() as DatagramChannel

    byteBuffer.clear()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        channel.read(byteBuffer)
        from = channel.remoteAddress
    } else {
        from = channel.receive(byteBuffer)
    }
    byteBuffer.flip()

    Log.d("SOCKET", String.format("Received %d bytes from %s", byteBuffer.limit(), from))
    //println(String.format("Received %d bytes from %s", byteBuffer.limit(), from))

    key.interestOps(SelectionKey.OP_WRITE)
}

private fun handleWrite(key: SelectionKey) {
    val channel =
        key.channel() as DatagramChannel

    if (from != null) {
        byteBuffer.clear()
        byteBuffer.putInt(1234)
        byteBuffer.flip()
        val bytes = channel.send(byteBuffer, from)
        Log.d("SOCKET", String.format("Send %d bytes to %s", bytes, from))
        //    println(String.format("Send %d bytes to %s", bytes, from))
    }

    key.interestOps(SelectionKey.OP_READ)
}