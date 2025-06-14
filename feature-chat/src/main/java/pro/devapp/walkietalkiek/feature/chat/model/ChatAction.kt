package pro.devapp.walkietalkiek.feature.chat.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel
import pro.devapp.walkietalkiek.serivce.network.data.model.MessageModel

internal sealed interface ChatAction {

    data object InitScreen : ChatAction
    data class ConnectedDevicesUpdated(
        val connectedDevices: List<ClientModel>
    ): ChatAction
    data class SendMessage(
        val content: String
    ): ChatAction
    data class MessagesReceived(
        val messages: List<MessageModel>
    ): ChatAction
    data class MarkMessageAsRead(
        val messageId: String
    ): ChatAction
    data class MessageSent(
        val message: ChatMessageModel
    ): ChatAction
    data class LoadChatHistory(
        val recipientId: String
    ): ChatAction
} 