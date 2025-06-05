package pro.devapp.walkietalkiek.feature.ptt.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

internal class ConnectedDevicesUpdatedReducer
    : Reducer<PttAction.ConnectedDevicesUpdated, PttScreenState, PttAction, PttEvent> {

    override val actionClass = PttAction.ConnectedDevicesUpdated::class

    override suspend fun reduce(
        action: PttAction.ConnectedDevicesUpdated,
        getState: () -> PttScreenState
    ): Reducer.Result<PttScreenState, PttAction, PttEvent?> {

        return Reducer.Result(
            state = getState().copy(
                connectedDevices = action.connectedDevices
            ),
            event = null
        )
    }

}