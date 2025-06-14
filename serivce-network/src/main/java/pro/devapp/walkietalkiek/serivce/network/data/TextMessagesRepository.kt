package pro.devapp.walkietalkiek.serivce.network.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel
import pro.devapp.walkietalkiek.serivce.network.data.model.MessageModel

class TextMessagesRepository(
    private val connectedDevicesRepository: ConnectedDevicesRepository
) {

    private val _messagesFlow = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: SharedFlow<List<MessageModel>>
        get() = _messagesFlow

    internal fun addMessage(
        message: String,
        hostAddress: String
    ) {
        val currentMessages = _messagesFlow.value.toMutableList()
        val client = connectedDevicesRepository.getClientByAddress(hostAddress) ?: ClientModel(
            hostAddress = hostAddress,
            hostName = "--",
            isConnected = false,
            port = 0,
            lastDataReceivedAt = System.currentTimeMillis()
        )
        val newMessage = MessageModel(
            clientModel = client,
            content = message,
            timestamp = System.currentTimeMillis()
        )
        currentMessages.add(newMessage)
        _messagesFlow.tryEmit(currentMessages)
    }
}