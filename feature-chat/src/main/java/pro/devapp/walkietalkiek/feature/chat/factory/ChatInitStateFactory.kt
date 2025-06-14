package pro.devapp.walkietalkiek.feature.chat.factory

import pro.devapp.walkietalkiek.core.mvi.InitStateFactory
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState

internal class ChatInitStateFactory: InitStateFactory<ChatScreenState> {
    override fun createInitState(): ChatScreenState {
        return ChatScreenState(
            isConnected = false,
            messages = emptyList(),
            connectedDevices = emptyList()
        )
    }
} 