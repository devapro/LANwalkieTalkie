package pro.devapp.walkietalkiek.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import pro.devapp.walkietalkiek.R

@Composable
fun RailTabs(
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        NavigationRailItem(
            selected = true,
            onClick = {

            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    text = "Home",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        NavigationRailItem(
            selected = false,
            onClick = {

            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    text = "Home",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        NavigationRailItem(
            selected = false,
            onClick = {

            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    text = "Home",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}