package pro.devapp.walkietalkiek.feature.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject
import pro.devapp.walkietalkiek.feature.chat.ChatViewModel
import pro.devapp.walkietalkiek.feature.chat.model.ChatAction
import pro.devapp.walkietalkiek.feature.chat.model.ChatMessageModel

@Composable
fun ChatTab() {

    val viewModel = koinInject<ChatViewModel>()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(ChatAction.InitScreen)
        viewModel.startCollectingConnectedDevices()
    }

    val sampleMessages = listOf(
        ChatMessageModel(
            id = "1",
            sender = "Alice",
            content = "Hey! Are you ready for the group chat?",
            timestamp = System.currentTimeMillis() - 300000, // 5 minutes ago
            isRead = true
        ),
        ChatMessageModel(
            id = "2",
            sender = "current_user",
            content = "Yes, I'm here! Just joined the network.",
            timestamp = System.currentTimeMillis() - 240000, // 4 minutes ago
            isRead = true
        ),
        ChatMessageModel(
            id = "3",
            sender = "Bob",
            content = "Great! This LAN walkie-talkie app is working perfectly for our team communication.",
            timestamp = System.currentTimeMillis() - 180000, // 3 minutes ago
            isRead = false
        ),
        ChatMessageModel(
            id = "4",
            sender = "current_user",
            content = "Indeed! The chat feature is really useful for coordinating when we're not using voice.",
            timestamp = System.currentTimeMillis() - 120000, // 2 minutes ago
            isRead = false
        )
    )

    ChatContent(
        messages = state.value.messages,
        currentUserId = "current_user",
        onSendMessage = { message ->
            viewModel.onAction(ChatAction.SendMessage(message))
        }
    )
}