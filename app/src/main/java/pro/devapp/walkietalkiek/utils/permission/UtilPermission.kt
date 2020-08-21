package pro.devapp.walkietalkiek.utils.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.collections.ArrayList

class UtilPermission {

    companion object {
        const val RESULT_CODE = 121
    }

    private val resultListeners = LinkedList<PermissionCallback>()

    fun openAppPermissionScreen(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri =
            Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun hasPermission(context: Context, @Permission permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            context,
            permission
        )
    }

    @Permission
    fun hasPermissions(context: Context, @Permission permissions: Array<String>): Array<String>? {
        val notGranted = ArrayList<String>()
        for (permission in permissions) {
            if (!hasPermission(context, permission)) notGranted.add(permission)
        }
        return if (notGranted.isEmpty()) null else notGranted.toTypedArray()
    }

    @Permission
    fun checkOrRequestPermissions(
        fragment: Fragment,
        callback: PermissionCallback
    ): Boolean {
        val requestPerms = ArrayList<String>()
        for (permission in callback.requestPerms) {
            if (!hasPermission(fragment.requireContext(), permission)) {
                requestPerms.add(permission)
            }
        }

        if (requestPerms.isNotEmpty()) {
            @Permission val perms = requestPerms.toTypedArray()
            ActivityCompat.requestPermissions(
                (fragment.activity as Activity), perms,
                RESULT_CODE
            )
            resultListeners.add(callback)
            return false
        }

        callback.onSuccessGrantedAll()
        return true
    }

    @Permission
    fun checkOrRequestPermissions(
        activity: Activity,
        callback: PermissionCallback
    ): Boolean {
        val requestPerms = ArrayList<String>()
        for (permission in callback.requestPerms) {
            if (!hasPermission(activity, permission)) {
                requestPerms.add(permission)
            }
        }

        if (requestPerms.isNotEmpty()) {
            @Permission val perms = requestPerms.toTypedArray()
            ActivityCompat.requestPermissions(
                activity, perms,
                RESULT_CODE
            )
            resultListeners.add(callback)
            return false
        }

        callback.onSuccessGrantedAll()
        return true
    }


    fun onRequestPermissionsResult(
        requestCode: Int?,
        @Permission permissions: Array<String>,
        grantResults: IntArray
    ) {
        val grantedPermissions =
            permissions.filterIndexed { index, _ -> grantResults[index] == PermissionChecker.PERMISSION_GRANTED }
        if (resultListeners.isNotEmpty() && requestCode == RESULT_CODE) {
            for (callback in resultListeners) {
                callback.result(grantedPermissions.toTypedArray())
            }
        }
    }

    interface IPermissionsResults {
        fun result(@Permission grantedPerms: Array<String>)
    }

    abstract class PermissionCallback(@Permission val requestPerms: Array<String>) :
        IPermissionsResults {

        override fun result(@Permission grantedPerms: Array<String>) {
            var grandAll = true
            for (permission in requestPerms) {
                if (grantedPerms.indexOf(permission) < 0) {
                    grandAll = false
                }
            }
            if (grandAll) {
                onSuccessGrantedAll()
            }
        }

        abstract fun onSuccessGrantedAll()
    }
}