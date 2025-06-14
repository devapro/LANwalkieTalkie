package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class MessageSentReducer: 
    Reducer<ChatAction.MessageSent, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.MessageSent::class

    override suspend fun reduce(
        action: ChatAction.MessageSent,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        return Reducer.Result(
            state = getState(), //update message status in the current state
            event = null
        )
    }
} 