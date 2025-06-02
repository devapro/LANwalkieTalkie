package pro.devapp.walkietalkiek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import org.koin.androidx.compose.getViewModel
import pro.devapp.walkietalkiek.core.theme.DroidPTTTheme
import pro.devapp.walkietalkiek.ui.MainViewMode
import pro.devapp.walkietalkiek.ui.components.BottomTabs
import pro.devapp.walkietalkiek.ui.components.RailTabs
import pro.devapp.walkietalkiek.ui.components.RootContent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewMode = getViewModel()
            val state = viewModel.state.collectAsState()

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
                        RootContent(
                            modifier = Modifier.padding(innerPadding),
                            state = state.value,
                            onAction = viewModel::onAction
                        )
                    }
                }
            }
        }
    }
}