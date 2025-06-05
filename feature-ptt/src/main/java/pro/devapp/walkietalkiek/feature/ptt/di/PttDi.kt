package pro.devapp.walkietalkiek.feature.ptt.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import pro.devapp.walkietalkiek.feature.ptt.PttActionProcessor
import pro.devapp.walkietalkiek.feature.ptt.PttViewModel
import pro.devapp.walkietalkiek.feature.ptt.factory.PttInitStateFactory
import pro.devapp.walkietalkiek.feature.ptt.reducer.ConnectedDevicesUpdatedReducer
import pro.devapp.walkietalkiek.feature.ptt.reducer.InitScreenReducer

fun Module.registerPttDi() {
    reducersDi()
    factoryDi()
    viewModelsDi()
}

private fun Module.factoryDi() {
    factoryOf(::PttInitStateFactory)
}

private fun Module.reducersDi() {
    factoryOf(::InitScreenReducer)
    factoryOf(::ConnectedDevicesUpdatedReducer)
    factory {
        PttActionProcessor(
            reducers = setOf(
                get(InitScreenReducer::class),
                get(ConnectedDevicesUpdatedReducer::class)
            ),
            initStateFactory = get(),
            coroutineContextProvider = get()
        )
    }
}

private fun Module.viewModelsDi() {
    viewModelOf(::PttViewModel)
}