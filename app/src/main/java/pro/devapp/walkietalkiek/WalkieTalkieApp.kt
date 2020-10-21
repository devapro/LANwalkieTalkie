package pro.devapp.walkietalkiek

import android.app.Application
import pro.devapp.modules.storage.ConnectedDevicesRepository
import pro.devapp.modules.storage.DeviceInfoRepository
import pro.devapp.walkietalkiek.di.ApplicationComponent
import pro.devapp.walkietalkiek.di.ApplicationModule
import pro.devapp.walkietalkiek.di.DaggerApplicationComponent
import pro.devapp.walkietalkiek.di.StorageModule
import timber.log.Timber
import timber.log.Timber.DebugTree

class WalkieTalkieApp : Application() {
    lateinit var deviceInfoRepository: DeviceInfoRepository
    lateinit var connectedDevicesRepository: ConnectedDevicesRepository
    lateinit var notificationController: NotificationController

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            //Timber.plant(CrashReportingTree())
        }

        applicationComponent = DaggerApplicationComponent.builder()
            .storageModule(StorageModule(this))
            .applicationModule(ApplicationModule(this))
            .build()

        deviceInfoRepository = DeviceInfoRepository(applicationContext)
        connectedDevicesRepository = ConnectedDevicesRepository()

        notificationController = NotificationController(applicationContext)
        notificationController.createNotificationChanel()
    }
}