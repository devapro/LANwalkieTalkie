package pro.devapp.walkietalkiek.app

import java.nio.ByteBuffer

interface IServer {
    fun initServer(): Int
    fun stop()
    fun sendMessage(byteBuffer: ByteBuffer)
}