package pro.devapp.walkietalkiek.feature.chat.model

data class ChatMessageModel(
    val id: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
