package pro.devapp.walkietalkiek.entities

data class MessageEntity(
    val id: Long,
    val from: String,
    val message: String
) : Entity