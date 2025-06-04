package pro.devapp.walkietalkiek.app

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import org.koin.android.ext.android.inject

class WalkieService: Service() {

    private val chanelController: ChanelController by inject()
    private val notificationController: NotificationController by inject()

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        chanelController.startDiscovery()
        setWakeLock()
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
        chanelController.stopDiscovery()
        releaseWakeLock()
    }

    private fun setWakeLock() {
        wakeLock =
            (getSystemService(POWER_SERVICE) as PowerManager).run {
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