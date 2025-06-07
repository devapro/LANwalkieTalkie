package pro.devapp.walkietalkiek.feature.ptt.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

internal class VoiceDataReceivedReducer : Reducer<PttAction.VoiceDataReceived, PttScreenState, PttAction, PttEvent> {

    override val actionClass = PttAction.VoiceDataReceived::class

    override suspend fun reduce(
        action: PttAction.VoiceDataReceived,
        getState: () -> PttScreenState
    ): Reducer.Result<PttScreenState, PttAction, PttEvent?> {
        return Reducer.Result(
            state = getState().copy(
                voiceData = action.voiceData,
            ),
            event = null
        )
    }

}