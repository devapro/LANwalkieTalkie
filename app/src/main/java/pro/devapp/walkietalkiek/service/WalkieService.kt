package pro.devapp.walkietalkiek.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.WalkieTalkieApp.Companion.CHANNEL_ID
import pro.devapp.walkietalkiek.ui.MainActivity

class WalkieService : Service() {

    companion object {
        const val NOTIFICATION_ID = 233
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        (application as WalkieTalkieApp).chanelController.startDiscovery()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as WalkieTalkieApp).chanelController.stopDiscovery()
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID)
        }
        return builder.setContentTitle(applicationContext.resources.getString(R.string.app_name))
//            .setContentText(timeStr)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setCustomContentView(customRemoteViews)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //  .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(null)
            .build()
    }
}