package pro.devapp.walkietalkiek.serivce.network.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel
import java.util.Date

/**
 * Store information about connected devices
 */
class ConnectedDevicesRepository {
    private val clients = HashMap<String, ClientModel>()
    private val clientsSubject = MutableSharedFlow<List<ClientModel>>(
        replay = 1,
        extraBufferCapacity = 10
    )

    private fun publishChanges() {
        val clientsList = clients.map { it.value }.toList()
        clientsSubject.tryEmit(clientsList)
    }

    fun addOrUpdateHostStateToConnected(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            true
        )
        publishChanges()
    }

    fun setHostDisconnected(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            false
        )
        publishChanges()
    }

    fun addHostInfo(hostAddress: String, name: String) {
        clients[hostAddress] = ClientModel(
            hostAddress,
            name,
            clients[hostAddress]?.isConnected ?: false
        )
        publishChanges()
    }

    fun storeDataReceivedTime(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            clients[hostAddress]?.isConnected ?: true,
            Date().time
        )
        publishChanges()
    }

    fun getConnectedDevicesList(): SharedFlow<List<ClientModel>> {
        return clientsSubject
    }
}