package pro.devapp.walkietalkiek.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.devapp.walkietalkiek.feature.chat.ui.ChatTab
import pro.devapp.walkietalkiek.feature.ptt.ui.PTTContent
import pro.devapp.walkietalkiek.feature.settings.SettingsContent
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenState
import pro.devapp.walkietalkiek.model.MainTab
import pro.devapp.walkietalkiek.ui.OffContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsContent(
    modifier: Modifier = Modifier,
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        when(state.currentTab) {
            MainTab.PTT -> {
                PTTContent()
            }
            MainTab.SETTINGS -> {
                SettingsContent()
            }
            MainTab.CHAT -> {
                ChatTab()
            }
            MainTab.OFF -> {
                 OffContent()
            }
        }
    }
}