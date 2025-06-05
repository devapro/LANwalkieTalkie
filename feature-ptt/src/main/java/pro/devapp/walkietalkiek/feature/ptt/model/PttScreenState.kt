package pro.devapp.walkietalkiek.feature.ptt.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel

internal data class PttScreenState(
    val isConnected: Boolean,
    val myIP: String,
    val connectedDevices: List<ClientModel>
)