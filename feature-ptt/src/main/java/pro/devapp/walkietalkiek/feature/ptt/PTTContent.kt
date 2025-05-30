package pro.devapp.walkietalkiek.feature.ptt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PTTContent(modifier: Modifier) {
    Column(
        modifier = modifier
    ) {


        MyDeviceInfo(
            isOnline = true,
            addressIp4 = "192.168.1.1",
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

        PTTButton(
            modifier = Modifier.padding(16.dp),
            isOnline = true,
            onClick = { /* Handle PTT button click */ }
        )

        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                // showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            DeviceItem(
                isOnline = true,
                address = "192.168.1.1"
            )

            DeviceItem(
                isOnline = true,
                address = "192.168.1.1"
            )
            // Sheet content
//            Button(onClick = {
//                scope.launch { sheetState.hide() }.invokeOnCompletion {
//                    if (!sheetState.isVisible) {
//                        showBottomSheet = false
//                    }
//                }
//            }) {
//                Text("Hide bottom sheet")
//            }
        }
    }
}