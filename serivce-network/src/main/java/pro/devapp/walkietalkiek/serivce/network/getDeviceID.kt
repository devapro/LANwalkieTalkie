package pro.devapp.walkietalkiek.serivce.network

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings

@SuppressLint("HardwareIds")
internal fun getDeviceID(contentResolver: ContentResolver): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}