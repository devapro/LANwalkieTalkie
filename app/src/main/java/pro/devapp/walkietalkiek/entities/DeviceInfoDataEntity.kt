package pro.devapp.walkietalkiek.entities

data class DeviceInfoDataEntity(
    val deviceId: String,
    val name: String,
    val port: Int
) : DataEntity