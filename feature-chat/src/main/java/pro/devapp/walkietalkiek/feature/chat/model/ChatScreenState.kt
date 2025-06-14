package pro.devapp.walkietalkiek.feature.chat.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel

internal data class ChatScreenState(
    val isConnected: Boolean,
    val connectedDevices: List<ClientModel>,
    val messages: List<ChatMessageModel>
)