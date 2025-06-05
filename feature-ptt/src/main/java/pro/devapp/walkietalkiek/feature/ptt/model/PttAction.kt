package pro.devapp.walkietalkiek.feature.ptt.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel

internal sealed interface PttAction {

    data object InitScreen : PttAction
    data class ConnectedDevicesUpdated(
        val connectedDevices: List<ClientModel>
    ): PttAction
}