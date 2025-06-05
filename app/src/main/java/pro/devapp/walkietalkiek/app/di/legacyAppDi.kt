package pro.devapp.walkietalkiek.app.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import pro.devapp.walkietalkiek.app.ChanelController
import pro.devapp.walkietalkiek.app.SocketClient
import pro.devapp.walkietalkiek.app.SocketServer

fun Module.registerLegacyAppDi() {
    singleOf(::SocketClient)
    singleOf(::SocketServer)
    singleOf(::ChanelController)
}