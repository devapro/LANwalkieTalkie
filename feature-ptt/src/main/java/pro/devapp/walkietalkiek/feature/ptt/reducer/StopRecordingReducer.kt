package pro.devapp.walkietalkiek.feature.ptt.reducer

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState
import pro.devapp.walkietalkiek.serivce.network.VoiceRecorder

internal class StopRecordingReducer(
    private val voiceRecorder: VoiceRecorder
)
    : Reducer<PttAction.StopRecording, PttScreenState, PttAction, PttEvent> {

    override val actionClass = PttAction.StopRecording::class

    override suspend fun reduce(
        action: PttAction.StopRecording,
        getState: () -> PttScreenState
    ): Reducer.Result<PttScreenState, PttAction, PttEvent?> {
        voiceRecorder.stopRecord()
        return Reducer.Result(
            state = getState(),
            event = null
        )
    }

}