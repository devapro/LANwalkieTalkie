package pro.devapp.walkietalkiek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import org.koin.android.ext.android.inject
import pro.devapp.walkietalkiek.app.NotificationController
import pro.devapp.walkietalkiek.ui.RootContent

class MainActivity : ComponentActivity() {

    private val notificationController: NotificationController by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RootContent()
        }

        initService()
    }

    private fun initService() {
        notificationController.createNotification()
    }
}