package pro.devapp.walkietalkiek.data

import android.content.Context
import android.os.Build
import pro.devapp.walkietalkiek.entities.DeviceInfoEntity
import pro.devapp.walkietalkiek.utils.getDeviceID

class DeviceInfoRepository(private val context: Context) {
    fun getCurrentDeviceInfo(): Result<DeviceInfoEntity> {
        val defaultName =
            Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + Build.VERSION.SDK_INT
        return Result.success(
            DeviceInfoEntity(
                getDeviceID(context.contentResolver),
                defaultName,
                10
            )
        )
    }
}