package pro.devapp.walkietalkiek.feature.chat

import pro.devapp.walkietalkiek.core.mvi.ActionProcessor
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.factory.ChatInitStateFactory
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class ChatActionProcessor(
    private val initStateFactory: ChatInitStateFactory,
    private val coroutineContextProvider: CoroutineContextProvider,
    reducers: Set<Reducer<ChatAction, ChatScreenState, ChatAction, ChatEvent>>,
): ActionProcessor<ChatScreenState, ChatAction, ChatEvent>(
    reducers = reducers,
    initStateFactory = initStateFactory,
    coroutineDispatcher = coroutineContextProvider.default
) 