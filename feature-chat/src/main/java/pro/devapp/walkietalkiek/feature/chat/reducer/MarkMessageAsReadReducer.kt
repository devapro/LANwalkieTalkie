package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class MarkMessageAsReadReducer: 
    Reducer<ChatAction.MarkMessageAsRead, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.MarkMessageAsRead::class

    override suspend fun reduce(
        action: ChatAction.MarkMessageAsRead,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val currentState = getState()
        
        val updatedMessages = currentState.messages.map { message ->
            if (message.id == action.messageId) {
                message.copy(isRead = true)
            } else {
                message
            }
        }
        
        return Reducer.Result(
            state = currentState.copy(messages = updatedMessages),
            event = null
        )
    }
} 