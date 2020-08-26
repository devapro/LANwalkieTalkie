package pro.devapp.walkietalkiek.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.ui.MainActivity

class NotificationController(private val context: Context) {

    companion object {
        const val NOTIFICATION_ID = 233
        const val CHANNEL_ID = "WalkieService"
    }

    fun createNotification(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(WalkieTalkieApp.CHANNEL_ID)
        }
        return builder.setContentTitle(context.resources.getString(R.string.app_name))
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

    fun createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "WalkieTalkie",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(false)
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                notificationChannel
            )
        }
    }
}