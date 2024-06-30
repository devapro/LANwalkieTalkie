package pro.devapp.walkietalkiek

import android.app.Application
import pro.devapp.walkietalkiek.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.data.DeviceInfoRepository
import pro.devapp.walkietalkiek.service.ChanelController
import pro.devapp.walkietalkiek.service.NotificationController
import timber.log.Timber
import timber.log.Timber.DebugTree

class WalkieTalkieApp : Application() {
    lateinit var chanelController: ChanelController
    lateinit var deviceInfoRepository: DeviceInfoRepository
    lateinit var connectedDevicesRepository: ConnectedDevicesRepository
    lateinit var notificationController: NotificationController

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        deviceInfoRepository = DeviceInfoRepository(applicationContext)
        connectedDevicesRepository = ConnectedDevicesRepository()

        notificationController = NotificationController(applicationContext)
        notificationController.createNotificationChanel()

        chanelController = ChanelController(
            applicationContext,
            deviceInfoRepository,
            connectedDevicesRepository
        )
    }
}