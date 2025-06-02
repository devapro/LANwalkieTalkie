package pro.devapp.walkietalkiek.app.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import pro.devapp.walkietalkiek.app.model.ClientEntity
import java.util.Date

/**
 * Store information about connected devices
 */
class ConnectedDevicesRepository {
    private val clients = HashMap<String, ClientEntity>()
    private val clientsSubject = MutableSharedFlow<List<ClientEntity>>(
        replay = 1,
        extraBufferCapacity = 10
    )

    private fun publishChanges() {
        val clientsList = clients.map { it.value }.toList()
        clientsSubject.tryEmit(clientsList)
    }

    fun addOrUpdateHostStateToConnected(hostAddress: String) {
        clients[hostAddress] = ClientEntity(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            true
        )
        publishChanges()
    }

    fun setHostDisconnected(hostAddress: String) {
        clients[hostAddress] = ClientEntity(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            false
        )
        publishChanges()
    }

    fun addHostInfo(hostAddress: String, name: String) {
        clients[hostAddress] = ClientEntity(
            hostAddress,
            name,
            clients[hostAddress]?.isConnected ?: false
        )
        publishChanges()
    }

    fun storeDataReceivedTime(hostAddress: String) {
        clients[hostAddress] = ClientEntity(
            hostAddress,
            clients[hostAddress]?.hostName ?: "",
            clients[hostAddress]?.isConnected ?: true,
            Date().time
        )
        publishChanges()
    }

    fun getConnectedDevicesList(): SharedFlow<List<ClientEntity>> {
        return clientsSubject
    }
}