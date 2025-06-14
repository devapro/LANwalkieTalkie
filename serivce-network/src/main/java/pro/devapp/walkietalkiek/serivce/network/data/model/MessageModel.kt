package pro.devapp.walkietalkiek.serivce.network.data.model

data class MessageModel(
    val clientModel: ClientModel,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)