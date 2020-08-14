package pro.devapp.walkietalkiek

import android.util.Log
import pro.devapp.walkietalkiek.service.ResolveListener
import java.lang.String
import java.net.InetSocketAddress
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

private val byteBuffer = ByteBuffer.allocate(256)
private fun handleRead(key: SelectionKey) {
    val channel = key.channel() as DatagramChannel

    byteBuffer.clear()
    val from = channel.read(byteBuffer)
    byteBuffer.flip()

    Log.i(
        ResolveListener.LOG_TAG,
        String.format("Received %d bytes from %s", byteBuffer.limit(), from)
    )

    key.interestOps(SelectionKey.OP_WRITE)
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