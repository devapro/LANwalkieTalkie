package pro.devapp.walkietalkiek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import pro.devapp.walkietalkiek.ui.components.BottomTabs
import pro.devapp.walkietalkiek.ui.components.RailTabs
import pro.devapp.walkietalkiek.ui.components.RootContent
import pro.devapp.walkietalkiek.ui.theme.DroidPTTTheme

private fun WindowSizeClass.isCompact() = windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
        windowHeightSizeClass == WindowHeightSizeClass.COMPACT

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidPTTTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    val adaptiveInfo = currentWindowAdaptiveInfo()
                    val windowSize = with(LocalDensity.current) {
                        currentWindowSize().toSize().toDpSize()
                    }

                    val navLayoutType = when {
                        adaptiveInfo.windowPosture.isTabletop -> NavigationSuiteType.NavigationBar
                        adaptiveInfo.windowSizeClass.isCompact() -> NavigationSuiteType.NavigationBar
                        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED &&
                                windowSize.width >= 1200.dp -> NavigationSuiteType.NavigationDrawer

                        else -> NavigationSuiteType.NavigationRail
                    }
                    NavigationSuiteScaffoldLayout(
                        layoutType = navLayoutType,
                        navigationSuite = {
                            when (navLayoutType) {
                                NavigationSuiteType.NavigationBar -> {
                                    BottomTabs()
                                }

                                NavigationSuiteType.NavigationRail -> {
                                    RailTabs(
                                        modifier = Modifier
                                            .padding(innerPadding)
                                    )
                                }

                                NavigationSuiteType.NavigationDrawer -> {

                                }
                            }
                        }
                    ) {
                        RootContent(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}