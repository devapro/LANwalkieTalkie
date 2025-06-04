package pro.devapp.walkietalkiek.feature.ptt.reducer

import android.Manifest
import androidx.annotation.RequiresPermission
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState
import pro.devapp.walkietalkiek.serivce.network.data.DeviceInfoRepository

internal class InitScreenReducer(
    private val deviceInfoRepository: DeviceInfoRepository
): Reducer<PttAction.InitScreen, PttScreenState, PttAction, PttEvent> {

    override val actionClass = PttAction.InitScreen::class

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun reduce(
        action: PttAction.InitScreen,
        getState: () -> PttScreenState
    ): Reducer.Result<PttScreenState, PttAction, PttEvent?> {
        val myIP = deviceInfoRepository.getCurrentIp()
        return Reducer.Result(
            state = getState().copy(
                myIP = myIP ?: "--",
            ),
            event = null
        )
    }

}