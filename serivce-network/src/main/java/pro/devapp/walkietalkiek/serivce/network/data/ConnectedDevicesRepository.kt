package pro.devapp.walkietalkiek.serivce.network.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel
import java.util.Date

/**
 * Store information about connected devices
 */
class ConnectedDevicesRepository {
    private val clients = HashMap<String, ClientModel>()

    private val _clientsFlow = MutableStateFlow<List<ClientModel>>(emptyList())
    val clientsSubject: SharedFlow<List<ClientModel>>
        get() = _clientsFlow

    private fun publishChanges() {
        val clientsList = clients.map { it.value }.toList()
        _clientsFlow.tryEmit(clientsList)
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
            clients[hostAddress]?.isConnected == true
        )
        publishChanges()
    }

    fun storeDataReceivedTime(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            clients[hostAddress]?.isConnected != false,
            Date().time
        )
        publishChanges()
    }
}