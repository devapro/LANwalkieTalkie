package pro.devapp.walkietalkiek.ui

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.devapp.walkietalkiek.service.WalkieService

@Composable
internal fun OffContent() {
    val activity = LocalActivity.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Stop WalkieTalkieK app")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val serviceIntent = Intent(activity, WalkieService::class.java)
                activity?.stopService(serviceIntent)
                activity?.finishAndRemoveTask()
            }
        ) {
            Text(text = "Exit WalkieTalkieK")
        }
    }
}