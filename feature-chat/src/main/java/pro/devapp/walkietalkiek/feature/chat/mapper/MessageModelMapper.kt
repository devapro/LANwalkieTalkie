package pro.devapp.walkietalkiek.feature.chat.mapper

import pro.devapp.walkietalkiek.feature.chat.model.ChatMessageModel
import pro.devapp.walkietalkiek.serivce.network.data.model.MessageModel

internal class MessageModelMapper {

    fun map(messages: List<MessageModel>): List<ChatMessageModel> {
        return messages.map { message ->
            ChatMessageModel(
                id = message.clientModel.hostAddress + message.clientModel.port + message.timestamp,
                content = message.content,
                sender = message.clientModel.hostAddress + message.clientModel.port,
                timestamp = message.timestamp,
                isRead = false
            )
        }
    }
}