package pro.devapp.walkietalkiek.serivce.network

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings
import android.util.Base64
import java.lang.Byte
import java.util.Random
import kotlin.ByteArray
import kotlin.Long
import kotlin.String
import kotlin.let

@SuppressLint("HardwareIds")
internal fun getDeviceID(contentResolver: ContentResolver): String {
    var deviceID: Long = 0
    val androidId = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ANDROID_ID
    )

    androidId?.let {
        deviceID = Random().nextLong()
    }

    val bb = ByteArray(java.lang.Long.SIZE / Byte.SIZE)
    for (idx in bb.size - 1 downTo 0) {
        bb[idx] = (deviceID and 0xFF).toByte()
        deviceID = deviceID shr Byte.SIZE
    }

    return Base64.encodeToString(
        bb,
        Base64.NO_PADDING or Base64.NO_WRAP
    )
}