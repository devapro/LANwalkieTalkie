package pro.devapp.walkietalkiek.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import pro.devapp.walkietalkiek.WalkieTalkieApp

class WalkieService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        (application as WalkieTalkieApp).chanelController.startDiscovery()
        setWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationController.NOTIFICATION_ID,
            (application as WalkieTalkieApp).notificationController.createNotification()
        )
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as WalkieTalkieApp).chanelController.stopDiscovery()
        releaseWakeLock()
    }

    private fun setWakeLock() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
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