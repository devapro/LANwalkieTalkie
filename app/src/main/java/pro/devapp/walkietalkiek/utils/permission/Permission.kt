package pro.devapp.walkietalkiek.utils.permission

import android.Manifest

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class Permission {
    companion object {
        var CONTACTS_READ = Manifest.permission.READ_CONTACTS
        var CONTACTS_WRITE = Manifest.permission.WRITE_CONTACTS
        var STORAGE_READ = Manifest.permission.READ_EXTERNAL_STORAGE
        var STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        var CAMERA = Manifest.permission.CAMERA
        var AUDIO_RECORD = Manifest.permission.RECORD_AUDIO
        var LOCATION_FINE = Manifest.permission.ACCESS_FINE_LOCATION
        var LOCATION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
        var PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        var FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE
    }
}
