package pro.devapp.walkietalkiek.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import org.koin.android.ext.android.inject
import pro.devapp.walkietalkiek.serivce.network.ClientController
import pro.devapp.walkietalkiek.service.voice.VoicePlayer
import pro.devapp.walkietalkiek.service.voice.VoiceRecorder

class WalkieService: Service() {

    private val chanelController: ClientController by inject()
    private val notificationController: NotificationController by inject()
    private val voiceRecorder: VoiceRecorder by inject()
    private val voicePlayer: VoicePlayer by inject()

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        chanelController.startDiscovery()
        setWakeLock()
        voicePlayer.create()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationController.createNotificationChanel()
        ServiceCompat.startForeground(
            this,
            NotificationController.NOTIFICATION_ID,
            notificationController.createNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceRecorder.stopRecord()
        voiceRecorder.destroy()
        voicePlayer.shutdown()
        chanelController.stopDiscovery()
        releaseWakeLock()
    }

    private fun setWakeLock() {
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "WalkieTalkyApp::ServiceWakelockTag"
                ).apply {
                    acquire(10*60*1000L /*10 minutes*/)
                }
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.release()
    }
}