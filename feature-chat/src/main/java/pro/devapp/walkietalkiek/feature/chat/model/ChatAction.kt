package pro.devapp.walkietalkiek.feature.chat.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel

internal sealed interface ChatAction {

    data object InitScreen : ChatAction
    data class ConnectedDevicesUpdated(
        val connectedDevices: List<ClientModel>
    ): ChatAction
    data class SendMessage(
        val content: String
    ): ChatAction
    data class MessageReceived(
        val message: ChatMessageModel
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