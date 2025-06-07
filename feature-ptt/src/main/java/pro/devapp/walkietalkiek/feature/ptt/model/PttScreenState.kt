package pro.devapp.walkietalkiek.feature.ptt.model

import pro.devapp.walkietalkiek.serivce.network.data.model.ClientModel

internal data class PttScreenState(
    val isConnected: Boolean,
    val myIP: String,
    val connectedDevices: List<ClientModel>,
    val voiceData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PttScreenState

        if (isConnected != other.isConnected) return false
        if (myIP != other.myIP) return false
        if (connectedDevices != other.connectedDevices) return false
        if (!voiceData.contentEquals(other.voiceData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isConnected.hashCode()
        result = 31 * result + myIP.hashCode()
        result = 31 * result + connectedDevices.hashCode()
        result = 31 * result + (voiceData?.contentHashCode() ?: 0)
        return result
    }
}