package pro.devapp.walkietalkiek.feature.chat

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository

internal class ChatViewModel(
    actionProcessor: ChatActionProcessor,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    // TODO: Add chat message repository/service when available
): MviViewModel<ChatScreenState, ChatAction, ChatEvent>(
    actionProcessor = actionProcessor
) {

    fun startCollectingConnectedDevices() {
        viewModelScope.launch {
            connectedDevicesRepository.clientsFlow.collect {
                onAction(ChatAction.ConnectedDevicesUpdated(it))
            }
        }
        // TODO: Collect incoming chat messages
        // viewModelScope.launch {
        //     chatMessageService.incomingMessages.collect { message ->
        //         onAction(ChatAction.MessageReceived(message))
        //     }
        // }
    }
} 