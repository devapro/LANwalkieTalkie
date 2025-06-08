package pro.devapp.walkietalkiek.feature.ptt.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import pro.devapp.walkietalkiek.feature.ptt.PttActionProcessor
import pro.devapp.walkietalkiek.feature.ptt.PttViewModel
import pro.devapp.walkietalkiek.feature.ptt.factory.PttInitStateFactory
import pro.devapp.walkietalkiek.feature.ptt.reducer.ConnectedDevicesUpdatedReducer
import pro.devapp.walkietalkiek.feature.ptt.reducer.InitScreenReducer
import pro.devapp.walkietalkiek.feature.ptt.reducer.StartRecordingReducer
import pro.devapp.walkietalkiek.feature.ptt.reducer.StopRecordingReducer
import pro.devapp.walkietalkiek.feature.ptt.reducer.VoiceDataReceivedReducer

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
    factoryOf(::StartRecordingReducer)
    factoryOf(::StopRecordingReducer)
    factoryOf(::VoiceDataReceivedReducer)
    factory {
        PttActionProcessor(
            reducers = setOf(
                get(InitScreenReducer::class),
                get(ConnectedDevicesUpdatedReducer::class),
                get(StartRecordingReducer::class),
                get(StopRecordingReducer::class),
                get(VoiceDataReceivedReducer::class)
            ),
            initStateFactory = get(),
            coroutineContextProvider = get()
        )
    }
}

private fun Module.viewModelsDi() {
    viewModelOf(::PttViewModel)
}