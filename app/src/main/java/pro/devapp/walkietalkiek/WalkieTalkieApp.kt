package pro.devapp.walkietalkiek

import android.app.Application
import pro.devapp.walkietalkiek.data.DeviceInfoRepository

class WalkieTalkieApp : Application() {
    lateinit var chanelController: ChanelController
    lateinit var deviceInfoRepository: DeviceInfoRepository

    override fun onCreate() {
        super.onCreate()
        deviceInfoRepository = DeviceInfoRepository(applicationContext)
        chanelController = ChanelController(applicationContext, deviceInfoRepository)
    }
}