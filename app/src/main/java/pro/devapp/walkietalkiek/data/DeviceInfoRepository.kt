package pro.devapp.walkietalkiek.data

import android.content.Context
import android.os.Build
import pro.devapp.walkietalkiek.entities.DeviceInfoDataEntity
import pro.devapp.walkietalkiek.utils.getDeviceID

class DeviceInfoRepository(private val context: Context) {
    fun getCurrentDeviceInfo(): Result<DeviceInfoDataEntity> {
        val defaultName =
            Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + Build.VERSION.SDK_INT
        return Result.success(
            DeviceInfoDataEntity(
                getDeviceID(context.contentResolver),
                defaultName,
                10
            )
        )
    }
}