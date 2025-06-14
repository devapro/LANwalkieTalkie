package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class MessageReceivedReducer: 
    Reducer<ChatAction.MessageReceived, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.MessageReceived::class

    override suspend fun reduce(
        action: ChatAction.MessageReceived,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val currentState = getState()
        
        return Reducer.Result(
            state = currentState.copy(
                messages = currentState.messages + action.message
            ),
            event = ChatEvent.ScrollToBottom
        )
    }
} 