package pro.devapp.walkietalkiek.feature.chat.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class LoadChatHistoryReducer: 
    Reducer<ChatAction.LoadChatHistory, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.LoadChatHistory::class

    override suspend fun reduce(
        action: ChatAction.LoadChatHistory,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val currentState = getState()
        
        // TODO: Load chat history from repository/database
        // For now, we'll just clear the messages for the new recipient
        // In a real implementation, this would load messages from storage
        
        return Reducer.Result(
            state = currentState.copy(
                messages = emptyList(), // Clear messages for new recipient
            ),
            event = null
        )
    }
} 