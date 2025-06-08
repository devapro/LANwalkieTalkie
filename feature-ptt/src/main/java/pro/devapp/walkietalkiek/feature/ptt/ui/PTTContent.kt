package pro.devapp.walkietalkiek.feature.ptt.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
        viewModel.startCollectingConnectedDevices()
    }

    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    if (windowSize.width > windowSize.height) {
        // Landscape mode
        PTTContentLandscape(
            state = state.value,
            onAction = viewModel::onAction
        )
    } else {
        // Portrait mode
        PTTContentPortrait(
            state = state.value,
            onAction = viewModel::onAction
        )
    }
}