package pro.devapp.walkietalkiek

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingDeque

class Client(private val executorService: ExecutorService) {
    private val selector = Selector.open()

    /**
     * Data for sending
     */
    private val outputQueue = LinkedBlockingDeque<ByteBuffer>()

    init {
        executorService.execute {
            initClient()
        }
    }

    fun addClient(addr: InetSocketAddress) {
        val socketChannel = SocketChannel.open(addr)
        socketChannel.configureBlocking(false)

        executorService.execute() {
            val sectionKey = socketChannel.register(selector, 0, null)
            val interestOps = sectionKey.interestOps()
            sectionKey.interestOps(interestOps or SelectionKey.OP_READ)
        }
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        outputQueue.add(byteBuffer)
    }

    private fun initClient() {
        while (selector.isOpen) {
            selector.select()
            val it = selector.selectedKeys().iterator()
            while (it.hasNext()) {
                val key = it.next()
                it.remove();
                if (!key.isValid) {
                    continue;
                }

                if (key.isReadable) {
                    val sc = key.channel() as SocketChannel
                    val buffer = ByteBuffer.allocate(sc.socket().receiveBufferSize)

                    val readCount = sc.read(buffer)
//                        if (readCount == -1) {
//                            key.channel().close()
//                            key.cancel()
//                            continue
//                        }
                    if (readCount > 0) {
                        println("new message: " + sc.socket().inetAddress.hostAddress)
                        val rspData = ByteArray(readCount)
                        System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
                        //  buffer.flip()
                        println("message: ${java.lang.String(rspData).trim()}")

                        if (outputQueue.isNotEmpty()) {
                            val outputBuff = outputQueue.pollFirst()
                            outputBuff?.let {
                                sc.write(it);
                            }
                        }

                        //TODO
                        sendMessage(ByteBuffer.wrap("received".toByteArray()))
                    }

                    //  key.interestOps(SelectionKey.OP_WRITE)
                }

            }
        }
    }
}