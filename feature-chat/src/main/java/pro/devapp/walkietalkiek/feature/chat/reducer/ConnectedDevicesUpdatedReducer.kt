package pro.devapp.walkietalkiek.feature.chat.reducer

import android.Manifest
import androidx.annotation.RequiresPermission
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository

internal class ConnectedDevicesUpdatedReducer(
    private val deviceInfoRepository: DeviceInfoRepository
): Reducer<ChatAction.ConnectedDevicesUpdated, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.ConnectedDevicesUpdated::class

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun reduce(
        action: ChatAction.ConnectedDevicesUpdated,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val myIP = deviceInfoRepository.getCurrentIp()
        val currentState = getState()
        return Reducer.Result(
            state = currentState.copy(
                connectedDevices = action.connectedDevices,
                isConnected = myIP != "-" && action.connectedDevices.isNotEmpty(),
            ),
            event = null
        )
    }
} 