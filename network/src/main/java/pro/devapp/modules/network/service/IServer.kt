package pro.devapp.modules.network.service

import java.nio.ByteBuffer

interface IServer {
    fun initServer(): Int
    fun stop()
    fun sendMessage(byteBuffer: ByteBuffer)
}