package pro.devapp.walkietalkiek.entities

data class DeviceInfoEntity(
    val deviceId: String,
    val name: String,
    val port: Int
) : Entity