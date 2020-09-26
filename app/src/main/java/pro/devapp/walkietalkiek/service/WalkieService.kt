package pro.devapp.walkietalkiek.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import io.reactivex.subjects.PublishSubject
import pro.devapp.walkietalkiek.WalkieTalkieApp
import timber.log.Timber
import java.nio.ByteBuffer

class WalkieService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var chanelController: ChanelController? = null

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        chanelController = ChanelController(
            applicationContext,
            (application as WalkieTalkieApp).deviceInfoRepository,
            (application as WalkieTalkieApp).connectedDevicesRepository
        ).apply { startDiscovery() }
        setWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationController.NOTIFICATION_ID,
            (application as WalkieTalkieApp).notificationController.createNotification()
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        chanelController?.stopDiscovery()
        chanelController = null
        releaseWakeLock()
    }

    private fun setWakeLock() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "WalkieTalkyApp::ServiceWakelockTag"
                ).apply {
                    acquire()
                }
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.release()
    }

    abstract class MBinder : Binder() {
        abstract fun sendMessage(byteBuffer: ByteBuffer)
        abstract fun getAudioData(): PublishSubject<ByteArray>?
    }

    private val binder = object : MBinder() {
        override fun sendMessage(byteBuffer: ByteBuffer) {
            Timber.i("sendMessage ${byteBuffer.array().size}")
            this@WalkieService.chanelController?.sendMessage(byteBuffer)
        }

        override fun getAudioData(): PublishSubject<ByteArray>? {
            return this@WalkieService.chanelController?.subjectAudioData
        }
    }
}