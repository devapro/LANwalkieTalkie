package pro.devapp.walkietalkiek.service

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import pro.devapp.walkietalkiek.ChanelController
import timber.log.Timber
import java.lang.String
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey


class RegistrationListener(private val chanelController: ChanelController) :
    NsdManager.RegistrationListener {
    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Log.e(
            "RegistrationListener",
            ": onRegistrationFailed: $serviceInfo ($errorCode)"
        )
    }

    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        Log.i(
            "RegistrationListener",
            ": onServiceUnregistered: $serviceInfo"
        )
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        Log.e(
            "RegistrationListener",
            ": onUnregistrationFailed: $serviceInfo ($errorCode)"
        )
    }

    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
        Timber.i(
            "onServiceRegistered: $serviceInfo"
        )
        //TODO check duplicate registration
        // after we can start connect to other services found
        //onServiceRegistered: name: SFVBV0VJIERVQS1MWDkgMTAgMjk:9dTeyFnNgNo:, type: null, host: null, port: 0, txtRecord:  &&&
        //chanelController.resolveService(serviceInfo)

        //  chanelController.acceptConnection()

        //     handleConnection(serviceInfo.port)
        //  handlerConnectionSocket()
        chanelController.onServiceRegister()
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
}