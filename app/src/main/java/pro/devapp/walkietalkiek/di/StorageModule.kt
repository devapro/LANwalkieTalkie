package pro.devapp.walkietalkiek.di

import android.app.Application
import dagger.Module
import dagger.Provides
import pro.devapp.modules.storage.ConnectedDevicesRepository
import pro.devapp.modules.storage.DeviceInfoRepository
import pro.devapp.modules.storage.MessagesRepository
import javax.inject.Singleton

@Module
class StorageModule(private val application: Application) {

    @Provides
    @Singleton
    fun deviceInfoRepository() = DeviceInfoRepository(application)

    @Provides
    @Singleton
    fun connectedDevicesRepository() = ConnectedDevicesRepository()

    @Provides
    @Singleton
    fun messagesRepository() = MessagesRepository()
}