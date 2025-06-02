package pro.devapp.walkietalkiek.app.model

data class ClientEntity(
    val hostAddress: String,
    val hostName: String = "",
    val isConnected: Boolean = false,
    val lastDataReceivedAt: Long = 0
)