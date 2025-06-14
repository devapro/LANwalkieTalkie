package pro.devapp.walkietalkiek.feature.chat.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import pro.devapp.walkietalkiek.feature.chat.ChatActionProcessor
import pro.devapp.walkietalkiek.feature.chat.ChatViewModel
import pro.devapp.walkietalkiek.feature.chat.factory.ChatInitStateFactory
import pro.devapp.walkietalkiek.feature.chat.mapper.MessageModelMapper
import pro.devapp.walkietalkiek.feature.chat.reducer.ConnectedDevicesUpdatedReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.InitScreenReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.LoadChatHistoryReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.MarkMessageAsReadReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.MessageReceivedReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.MessageSentReducer
import pro.devapp.walkietalkiek.feature.chat.reducer.SendMessageReducer

fun Module.registerChatDi() {
    reducersDi()
    factoryDi()
    mapperDi()
    viewModelsDi()
}

private fun Module.factoryDi() {
    factoryOf(::ChatInitStateFactory)
}

private fun Module.reducersDi() {
    factoryOf(::InitScreenReducer)
    factoryOf(::ConnectedDevicesUpdatedReducer)
    factoryOf(::SendMessageReducer)
    factoryOf(::MessageReceivedReducer)
    factoryOf(::MessageSentReducer)
    factoryOf(::LoadChatHistoryReducer)
    factoryOf(::MarkMessageAsReadReducer)
    factory {
        ChatActionProcessor(
            reducers = setOf(
                get(InitScreenReducer::class),
                get(ConnectedDevicesUpdatedReducer::class),
                get(SendMessageReducer::class),
                get(MessageReceivedReducer::class),
                get(MessageSentReducer::class),
                get(LoadChatHistoryReducer::class),
                get(MarkMessageAsReadReducer::class)
            ),
            initStateFactory = get(),
            coroutineContextProvider = get()
        )
    }
}

private fun Module.mapperDi() {
    factoryOf(::MessageModelMapper)
}

private fun Module.viewModelsDi() {
    viewModelOf(::ChatViewModel)
} 