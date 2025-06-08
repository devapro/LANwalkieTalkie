package pro.devapp.walkietalkiek.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenState

@Composable
fun RailTabs(
    modifier: Modifier = Modifier,
    screenState: MainScreenState,
    onAction: (MainScreenAction) -> Unit = {}
) {
    val windowInsets = WindowInsets.navigationBars
    NavigationRail(
        modifier = modifier,
        windowInsets = windowInsets
    ) {
        screenState.mainTabs.forEach {
            NavigationRailItem(
                selected = it.screen == screenState.currentTab,
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