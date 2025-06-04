package pro.devapp.walkietalkiek.feature.ptt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.koin.compose.koinInject
import pro.devapp.walkietalkiek.feature.ptt.PttViewModel
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTTContent(
    modifier: Modifier = Modifier
) {
    val viewModel: PttViewModel = koinInject()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(PttAction.InitScreen)
    }

    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    if (windowSize.width > windowSize.height) {
        // Landscape mode
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceItem(
                    isOnline = true,
                    address = "192.168.1.1"
                )

                DeviceItem(
                    isOnline = true,
                    address = "192.168.1.1"
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyDeviceInfo(
                    isOnline = state.value.isConnected,
                    addressIp4 = state.value.myIP,
                    addressIp6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
                )
                Box(
                    modifier = modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PTTButton(
                        modifier = Modifier
                            .width(150.dp)
                            .padding(8.dp),
                        isOnline = state.value.isConnected,
                        onClick = { /* Handle PTT button click */ }
                    )
                }
                VoiceDiagram()
            }
        }
    } else {
        // Portrait mode
        Column {

            MyDeviceInfo(
                isOnline = state.value.isConnected,
                addressIp4 = state.value.myIP,
                addressIp6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
            )

            DeviceItem(
                isOnline = true,
                address = "192.168.1.1"
            )

            DeviceItem(
                isOnline = true,
                address = "192.168.1.1"
            )

            Box(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PTTButton(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(8.dp),
                    isOnline = true,
                    onClick = { /* Handle PTT button click */ }
                )
            }

            VoiceDiagram()
        }
    }
}