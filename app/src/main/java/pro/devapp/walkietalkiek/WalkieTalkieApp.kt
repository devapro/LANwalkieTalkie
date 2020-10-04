package pro.devapp.walkietalkiek

import android.app.Application
import pro.devapp.modules.storage.ConnectedDevicesRepository
import pro.devapp.modules.storage.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.NotificationController
import timber.log.Timber
import timber.log.Timber.DebugTree

class WalkieTalkieApp : Application() {
    lateinit var deviceInfoRepository: DeviceInfoRepository
    lateinit var connectedDevicesRepository: ConnectedDevicesRepository
    lateinit var notificationController: NotificationController

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            //Timber.plant(CrashReportingTree())
        }
        deviceInfoRepository = DeviceInfoRepository(applicationContext)
        connectedDevicesRepository = ConnectedDevicesRepository()

        notificationController = NotificationController(applicationContext)
        notificationController.createNotificationChanel()
    }
}