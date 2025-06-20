package pro.devapp.walkietalkiek.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pro.devapp.walkietalkiek.MainViewMode
import pro.devapp.walkietalkiek.PermissionState
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.factory.MainScreenInitStateFactory
import pro.devapp.walkietalkiek.factory.MainTabsFactory
import pro.devapp.walkietalkiek.feature.chat.di.registerChatDi
import pro.devapp.walkietalkiek.feature.ptt.di.registerPttDi
import pro.devapp.walkietalkiek.reducers.ChangeScreenReducer
import pro.devapp.walkietalkiek.reducers.CheckPermissionsReducer
import pro.devapp.walkietalkiek.reducers.InitAppReducer
import pro.devapp.walkietalkiek.reducers.MainActionProcessor
import pro.devapp.walkietalkiek.serivce.network.di.registerServiceNetworkDi
import pro.devapp.walkietalkiek.service.NotificationController
import pro.devapp.walkietalkiek.service.voice.di.registerVoiceDi

val appModule: Module = module {
    coreDi()
    factoriesDi()
    viewModelsDi()
    reducersDi()
    registerPttDi()
    registerServiceNetworkDi()
    registerVoiceDi()
    registerChatDi()
}

private fun Module.coreDi() {
    factoryOf(::CoroutineContextProvider)
    factoryOf(::PermissionState)
    factoryOf(::NotificationController)
}

private fun Module.factoriesDi() {
    factoryOf(::MainScreenInitStateFactory)
    factoryOf(::MainTabsFactory)
}

private fun Module.viewModelsDi() {
    viewModelOf(::MainViewMode)
}

private fun Module.reducersDi() {
    factoryOf(::ChangeScreenReducer)
    factoryOf(::InitAppReducer)
    factoryOf(::CheckPermissionsReducer)
    factory {
        MainActionProcessor(
            reducers = setOf(
                get(ChangeScreenReducer::class),
                get(InitAppReducer::class),
                get(CheckPermissionsReducer::class)
            ),
            initStateFactory = get(),
            coroutineContextProvider = get()
        )
    }
}