package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatMessageModel
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import pro.devapp.walkietalkiek.serivce.network.MessageController
import java.nio.ByteBuffer
import java.util.UUID

internal class SendMessageReducer(
    private val messageController: MessageController
): Reducer<ChatAction.SendMessage, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.SendMessage::class

    override suspend fun reduce(
        action: ChatAction.SendMessage,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val currentState = getState()

        messageController.sendMessage(
            ByteBuffer.wrap(action.content.toByteArray())
        )

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