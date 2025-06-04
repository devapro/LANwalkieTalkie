package pro.devapp.walkietalkiek.app.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import pro.devapp.walkietalkiek.app.ChanelController
import pro.devapp.walkietalkiek.app.SocketClient
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository

fun Module.registerLegacyAppDi() {
    singleOf(::ConnectedDevicesRepository)
    singleOf(::DeviceInfoRepository)
    singleOf(::SocketClient)
    singleOf(::ChanelController)
}