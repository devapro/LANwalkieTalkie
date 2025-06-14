package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.mapper.MessageModelMapper
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class MessageReceivedReducer(
    private val messageModelMapper: MessageModelMapper
):
    Reducer<ChatAction.MessagesReceived, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.MessagesReceived::class

    override suspend fun reduce(
        action: ChatAction.MessagesReceived,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val updatedMessages = messageModelMapper.map(
            action.messages
        )
        val currentState = getState()
        val allMessages = currentState.messages + updatedMessages
        
        return Reducer.Result(
            state = currentState.copy(
                messages = allMessages.distinctBy {
                    it.id
                },
            ),
            event = ChatEvent.ScrollToBottom
        )
    }
} 