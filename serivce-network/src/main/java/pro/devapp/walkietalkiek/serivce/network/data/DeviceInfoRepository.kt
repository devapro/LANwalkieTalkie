package pro.devapp.walkietalkiek.serivce.network.data

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import pro.devapp.walkietalkiek.serivce.network.data.model.DeviceInfoModel
import pro.devapp.walkietalkiek.serivce.network.getDeviceID
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class DeviceInfoRepository(private val context: Context) {
    fun getCurrentDeviceInfo(): DeviceInfoModel {
        val defaultName =
            Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + Build.VERSION.SDK_INT
        return DeviceInfoModel(
            getDeviceID(context.contentResolver),
            defaultName,
            10
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getCurrentIp(): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetwork?.let { network ->
            val linkProperties = connectivityManager.getLinkProperties(network)
            return linkProperties?.linkAddresses?.filter { it.address.isLoopbackAddress.not() }
                ?.joinToString("\n") { linkAddress ->
                    linkAddress.address.hostAddress
                }
        }
        val wifiManager = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        var ipAddress = wifiManager.connectionInfo.ipAddress

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

        return try {
            InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (ex: UnknownHostException) {
            null
        }
    }
}