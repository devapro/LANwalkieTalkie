package pro.devapp.walkietalkiek

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.ChanelController
import timber.log.Timber
import timber.log.Timber.DebugTree

class WalkieTalkieApp : Application() {
    lateinit var chanelController: ChanelController
    lateinit var deviceInfoRepository: DeviceInfoRepository

    companion object {
        const val CHANNEL_ID = "WalkieService"
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            //Timber.plant(CrashReportingTree())
        }
        deviceInfoRepository = DeviceInfoRepository(applicationContext)
        chanelController = ChanelController(
            applicationContext,
            deviceInfoRepository
        )

        createNotificationChanel()
    }

    private fun createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "WalkieTalkie",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(false)
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                notificationChannel
            )
        }
    }
}