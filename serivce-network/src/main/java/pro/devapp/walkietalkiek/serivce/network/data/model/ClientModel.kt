package pro.devapp.walkietalkiek.serivce.network.data.model

data class ClientModel(
    val hostAddress: String,
    val hostName: String = "",
    val isConnected: Boolean = false,
    val port: Int = 0,
    val lastDataReceivedAt: Long = 0
)