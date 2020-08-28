package pro.devapp.walkietalkiek.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import pro.devapp.walkietalkiek.WalkieTalkieApp

class WalkieService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        (application as WalkieTalkieApp).chanelController.startDiscovery()
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
    }
}