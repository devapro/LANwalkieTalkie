package pro.devapp.walkietalkiek.serivce.network.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import pro.devapp.walkietalkiek.serivce.network.ChanelController
import pro.devapp.walkietalkiek.serivce.network.ClientInfoResolver
import pro.devapp.walkietalkiek.serivce.network.SocketClient
import pro.devapp.walkietalkiek.serivce.network.SocketServer
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository

fun Module.registerServiceNetworkDi() {
    factoryOf(::ClientInfoResolver)
    factoryOf(::DeviceInfoRepository)
    singleOf(::ConnectedDevicesRepository)

    singleOf(::SocketClient)
    singleOf(::SocketServer)
    singleOf(::ChanelController)
}