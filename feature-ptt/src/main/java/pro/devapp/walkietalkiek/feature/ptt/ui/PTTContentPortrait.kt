package pro.devapp.walkietalkiek.feature.ptt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

@Composable
internal fun PTTContentPortrait(
    state: PttScreenState,
    onAction: (PttAction) -> Unit,
) {
    Column {

        MyDeviceInfo(
            isOnline = state.isConnected,
            addressIp = state.myIP
        )

        state.connectedDevices.forEach {
            DeviceItem(
                isOnline = it.isConnected,
                address = "${it.hostAddress}:${it.port}"
            )
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PTTButton(
                modifier = Modifier
                    .width(150.dp)
                    .padding(8.dp),
                isOnline = state.isConnected,
                onPress = {
                    onAction(PttAction.StartRecording)
                },
                onRelease = {
                    onAction(PttAction.StopRecording)
                }
            )
        }

        WaveCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            data = state.voiceData,
        )
        Spacer(
            modifier = Modifier.height(16.dp)
        )
    }
}