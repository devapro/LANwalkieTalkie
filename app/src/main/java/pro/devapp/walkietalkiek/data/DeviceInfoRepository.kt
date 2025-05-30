package pro.devapp.walkietalkiek.data

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Build
import pro.devapp.walkietalkiek.entities.DeviceInfoEntity
import pro.devapp.walkietalkiek.utils.getDeviceID
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class DeviceInfoRepository(private val context: Context) {
    fun getCurrentDeviceInfo(): DeviceInfoEntity {
        val defaultName =
            Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + Build.VERSION.SDK_INT
        return DeviceInfoEntity(
            getDeviceID(context.contentResolver),
            defaultName,
            10
        )
    }

    fun getCurrentIp(): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork?.let { network ->
                val linkProperties = connectivityManager.getLinkProperties(network)
                return linkProperties?.linkAddresses?.filter { it.address.isLoopbackAddress.not() }?.map { linkAddress ->
                    linkAddress.address.hostAddress
                }?.joinToString("\n")
            }
        }
        val wifiManager = (context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
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