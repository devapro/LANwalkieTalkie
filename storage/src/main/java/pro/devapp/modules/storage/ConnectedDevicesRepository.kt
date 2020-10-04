package pro.devapp.modules.storage

import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import pro.devapp.modules.data.entities.ClientEntity
import java.util.*
import kotlin.collections.HashMap

/**
 * Store information about connected devices
 */
class ConnectedDevicesRepository {
    private val clients = HashMap<String, ClientEntity>()
    private val clientsSubject = ReplaySubject.createWithSize<List<ClientEntity>>(1)
        .apply {
            subscribeOn(
                Schedulers.io()
            )
        }

    private fun publishChanges() {
        val clientsList = clients.map { it.value }.toList()
        if (!clientsSubject.hasValue() || clientsSubject.hasObservers())
            clientsSubject.onNext(clientsList)
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

    fun getConnectedDevicesList(): Subject<List<ClientEntity>> {
        return clientsSubject
    }
}