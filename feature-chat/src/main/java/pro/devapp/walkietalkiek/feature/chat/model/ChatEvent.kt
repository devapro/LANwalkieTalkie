package pro.devapp.walkietalkiek.feature.chat.model

internal sealed interface ChatEvent {
    data object ScrollToBottom : ChatEvent
    data class MessageSendFailed(val error: String) : ChatEvent
} 