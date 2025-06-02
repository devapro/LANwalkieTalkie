package pro.devapp.walkietalkiek.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenState

@Composable
fun BottomTabs(
    modifier: Modifier = Modifier,
    screenState: MainScreenState,
    onAction: (MainScreenAction) -> Unit = {}
) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        windowInsets = WindowInsets.navigationBars
    ) {
        screenState.mainTabs.forEach {
            NavigationBarItem(
                selected = it.screen == screenState.currentTab,
                enabled = true,
                onClick = {
                    onAction(
                        MainScreenAction.ChangeScreen(
                            it.screen
                        )
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = it.title
                    )
                },
                label = {
                    Text(
                        text = it.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}