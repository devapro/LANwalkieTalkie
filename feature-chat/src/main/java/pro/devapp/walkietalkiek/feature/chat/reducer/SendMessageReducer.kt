package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatMessageModel
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import java.util.UUID

internal class SendMessageReducer: 
    Reducer<ChatAction.SendMessage, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.SendMessage::class

    override suspend fun reduce(
        action: ChatAction.SendMessage,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val currentState = getState()

        val newMessage = ChatMessageModel(
            id = UUID.randomUUID().toString(),
            sender = "me",
            content = action.content,
            timestamp = System.currentTimeMillis(),
            isRead = true // Own messages are always read
        )

        return Reducer.Result(
            state = currentState.copy(
                messages = currentState.messages + newMessage
            ),
            event = ChatEvent.ScrollToBottom
        )
    }
} 