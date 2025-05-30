package pro.devapp.walkietalkiek.feature.ptt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DeviceItem(
    modifier: Modifier = Modifier,
    isOnline: Boolean,
    address: String
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.scrim,
                    shape = CircleShape
                )
        )
        Text(
            text = address,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}