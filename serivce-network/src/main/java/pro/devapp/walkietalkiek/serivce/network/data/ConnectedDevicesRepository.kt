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

    fun addOrUpdateHostStateToConnected(hostAddress: String, port: Int) {
        clients[hostAddress] = ClientModel(
            hostAddress = hostAddress,
            isConnected = true,
            hostName = clients[hostAddress]?.hostName.orEmpty(),
            port = port,
            lastDataReceivedAt = Date().time
        )
        publishChanges()
    }

    fun setHostDisconnected(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress = hostAddress,
            isConnected = false,
            hostName = clients[hostAddress]?.hostName.orEmpty(),
            port = clients[hostAddress]?.port ?: 0,
            lastDataReceivedAt = clients[hostAddress]?.lastDataReceivedAt ?: 0,
        )
        publishChanges()
    }

    fun addHostInfo(hostAddress: String, name: String) {
        clients[hostAddress] = ClientModel(
            hostAddress = hostAddress,
            hostName = name,
            isConnected = clients[hostAddress]?.isConnected == true,
            port = clients[hostAddress]?.port ?: 0,
            lastDataReceivedAt = clients[hostAddress]?.lastDataReceivedAt ?: 0,
        )
        publishChanges()
    }

    fun storeDataReceivedTime(hostAddress: String) {
        clients[hostAddress] = ClientModel(
            hostAddress = hostAddress,
            hostName = clients[hostAddress]?.hostName.orEmpty(),
            isConnected = clients[hostAddress]?.isConnected != false,
            port = clients[hostAddress]?.port ?: 0,
            lastDataReceivedAt = clients[hostAddress]?.lastDataReceivedAt ?: 0,
        )
        publishChanges()
    }
}