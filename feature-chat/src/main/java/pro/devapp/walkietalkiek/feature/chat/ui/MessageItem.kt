package pro.devapp.walkietalkiek.feature.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pro.devapp.walkietalkiek.core.theme.onPrimaryLight
import pro.devapp.walkietalkiek.core.theme.onSurfaceLight
import pro.devapp.walkietalkiek.core.theme.primaryLight
import pro.devapp.walkietalkiek.core.theme.secondaryLight
import pro.devapp.walkietalkiek.core.theme.surfaceLight
import pro.devapp.walkietalkiek.feature.chat.model.ChatMessageModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MessageItem(
    message: ChatMessageModel,
    isOutgoing: Boolean
) {
    val alignment: Alignment.Horizontal = if (isOutgoing) Alignment.End else Alignment.Start
    val backgroundColor = if (isOutgoing) primaryLight else surfaceLight
    val textColor = if (isOutgoing) onPrimaryLight else onSurfaceLight

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(
                    start = if (isOutgoing) 48.dp else 0.dp,
                    end = if (isOutgoing) 0.dp else 48.dp
                ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOutgoing) 16.dp else 4.dp,
                bottomEnd = if (isOutgoing) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isOutgoing) {
                    Text(
                        text = message.sender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryLight,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = formatTimestamp(message.timestamp),
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (message.isRead) "✓✓" else "✓",
                            fontSize = 11.sp,
                            color = if (message.isRead) secondaryLight else textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m" // Less than 1 hour
        diff < 86400_000 -> { // Less than 24 hours
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        else -> { // More than 24 hours
            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }
}