package pro.devapp.walkietalkiek.feature.chat.reducer

import android.Manifest
import androidx.annotation.RequiresPermission
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository

internal class InitScreenReducer(
    private val deviceInfoRepository: DeviceInfoRepository
): Reducer<ChatAction.InitScreen, ChatScreenState, ChatAction, ChatEvent> {

    override val actionClass = ChatAction.InitScreen::class

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun reduce(
        action: ChatAction.InitScreen,
        getState: () -> ChatScreenState
    ): Reducer.Result<ChatScreenState, ChatAction, ChatEvent?> {
        val myIP = deviceInfoRepository.getCurrentIp()
        return Reducer.Result(
            state = getState().copy(
                messages = emptyList(),
                connectedDevices = getState().connectedDevices,
                isConnected = myIP != null && getState().connectedDevices.isNotEmpty()
            ),
            event = null
        )
    }
} 