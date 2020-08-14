package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import pro.devapp.walkietalkiek.ChanelController
import java.lang.String
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.text.toByteArray


class ResolveListener(private val chanelController: ChanelController) : NsdManager.ResolveListener {

    companion object {
        const val LOG_TAG = "ResolveListener"
    }

    private val executor = Executors.newCachedThreadPool()

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Log.i(
            LOG_TAG,
            ": onResolveFailed: $serviceInfo errorCode=$errorCode"
        )
        //TODO remove from list
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.i(LOG_TAG, ": onServiceResolved: $serviceInfo");

        //TODO add to list
        val addr = InetSocketAddress(serviceInfo.host, serviceInfo.port)
        Log.d("ResolveListener", "onServiceResolved: $addr")
        if (!addr.address.isMulticastAddress) {
            //handleConnection(addr)
            handleSocketConnection(addr)
        }
    }

    private val selector = Selector.open()
    private fun handleSocketConnection(addr: InetSocketAddress) {
        val socketChannel = SocketChannel.open(addr)
        socketChannel.configureBlocking(false)

        executor.execute() {
            val sectionKey = socketChannel.register(selector, 0, null)
            val interestOps = sectionKey.interestOps()
            sectionKey.interestOps(interestOps or SelectionKey.OP_READ)
        }

        startHandler()

//        executor.execute() {
////            val sectionKey = socketChannel.register( selector, 0, null )
////            val  interestOps = sectionKey.interestOps()
////            sectionKey.interestOps( interestOps or SelectionKey.OP_READ )
//            while (true) {
////                    if(sectionKey.isReadable){
////                        val sc =
////                            sectionKey.channel() as SocketChannel
////                        val data = ByteBuffer.allocate(sc.socket().sendBufferSize)
////                        println("new message: " + sc.socket().inetAddress.hostAddress)
////                    }
//                //socketChannel.read(byteBuffer)
//
//                try {
//                    //   val buffer = ByteBuffer.wrap(String("test").bytes)
//                    if (socketChannel.isConnected) {
//                        // socketChannel.write(buffer)
//
//                        val buffer = ByteBuffer.allocate(socketChannel.socket().receiveBufferSize)
//                        //      println("new message: " + socketChannel.socket().inetAddress.hostAddress)
//                        val readCount = socketChannel.read(buffer)
//                        if (readCount > 0) {
//                            val rspData = ByteArray(readCount)
//                            System.arraycopy(buffer.array(), 0, rspData, 0, readCount)
//                            //  buffer.flip()
//                            println("message: ${kotlin.text.String(rspData).trim()}")
//                        }
//                    } else {
//                        // executor.shutdown()
//                        socketChannel.close()
//                        break
//                    }
//                    //    buffer.clear()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                //  Log.d("ResolveListener", "sending....")
//                // Thread.sleep(2000)
//            }
//
//        }
    }

    private val isHandlerStarted = AtomicBoolean(false)

    private fun startHandler() {
        if (isHandlerStarted.compareAndSet(false, true)) {
            executor.execute {
                while (true) {
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
                                println("message: ${String(rspData).trim()}")

                                sc.write(ByteBuffer.wrap("received".toByteArray()));
                            }

                            //  key.interestOps(SelectionKey.OP_WRITE)
                        }

                    }
                }
            }
        }
    }


}