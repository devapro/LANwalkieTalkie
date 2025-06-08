package pro.devapp.walkietalkiek.model

data class MainScreenState(
    val currentTab: MainTab,
    val requiredPermissions: List<String>,
    val mainTabs: List<MainTabItem>
)