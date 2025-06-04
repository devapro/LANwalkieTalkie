package pro.devapp.walkietalkiek.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import org.koin.androidx.compose.getViewModel
import pro.devapp.walkietalkiek.MainViewMode
import pro.devapp.walkietalkiek.core.theme.DroidPTTTheme
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.ui.components.BottomTabs
import pro.devapp.walkietalkiek.ui.components.RailTabs
import pro.devapp.walkietalkiek.ui.components.RequiredPermissionsNotification
import pro.devapp.walkietalkiek.ui.components.TabsContent

@Composable
internal fun RootContent() {
    val viewModel: MainViewMode = getViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(MainScreenAction.InitApp)
    }

    DroidPTTTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            val windowSize = with(LocalDensity.current) {
                currentWindowSize().toSize().toDpSize()
            }

            val navLayoutType = if (windowSize.width > windowSize.height) {
                // Landscape mode
                NavigationSuiteType.NavigationRail
            } else {
                // Portrait mode
                NavigationSuiteType.NavigationBar
            }

            NavigationSuiteScaffoldLayout(
                layoutType = navLayoutType,
                navigationSuite = {
                    when (navLayoutType) {
                        NavigationSuiteType.NavigationBar -> {
                            BottomTabs(
                                screenState = state.value,
                                onAction = viewModel::onAction
                            )
                        }

                        NavigationSuiteType.NavigationRail -> {
                            RailTabs(
                                modifier = Modifier
                                    .padding(innerPadding),
                                screenState = state.value,
                                onAction = viewModel::onAction
                            )
                        }

                        NavigationSuiteType.NavigationDrawer -> {

                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabsContent(
                        state = state.value,
                        onAction = viewModel::onAction
                    )
                    if (state.value.requiredPermissions.isNotEmpty()) {
                        RequiredPermissionsNotification(
                            requiredPermissions = state.value.requiredPermissions,
                            onClick = {

                            }
                        )
                    }
                }
            }
        }
    }
}