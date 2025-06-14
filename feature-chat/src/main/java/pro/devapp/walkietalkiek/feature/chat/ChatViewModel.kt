package pro.devapp.walkietalkiek.feature.chat

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatEvent
import pro.devapp.walkietalkiek.feature.chat.model.ChatScreenState
import pro.devapp.walkietalkiek.serivce.network.data.ConnectedDevicesRepository
import pro.devapp.walkietalkiek.serivce.network.data.TextMessagesRepository

internal class ChatViewModel(
    actionProcessor: ChatActionProcessor,
    private val connectedDevicesRepository: ConnectedDevicesRepository,
    private val textMessagesRepository: TextMessagesRepository
): MviViewModel<ChatScreenState, ChatAction, ChatEvent>(
    actionProcessor = actionProcessor
) {

    fun startCollectingConnectedDevices() {
        viewModelScope.launch {
            connectedDevicesRepository.clientsFlow.collect {
                onAction(ChatAction.ConnectedDevicesUpdated(it))
            }
        }
         viewModelScope.launch {
             textMessagesRepository.messages.collect { message ->
                 onAction(ChatAction.MessagesReceived(message))
             }
         }
    }
} 