package pro.devapp.walkietalkiek.feature.ptt

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.service.voice.VoicePlayer

internal class PttViewModel(
    actionProcessor: PttActionProcessor,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val voicePlayer: VoicePlayer
): MviViewModel<PttScreenState, PttAction, PttEvent>(
    actionProcessor = actionProcessor
) {

    fun startCollectingConnectedDevices() {
        viewModelScope.launch {
            connectedDevicesRepository.clientsSubject.collect {
                onAction(PttAction.ConnectedDevicesUpdated(it))
            }
        }
        viewModelScope.launch {
            voicePlayer.voiceDataFlow.collect {
                onAction(PttAction.VoiceDataReceived(it))
            }
        }
    }
}