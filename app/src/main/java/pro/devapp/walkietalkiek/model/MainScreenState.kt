package pro.devapp.walkietalkiek.model

data class MainScreenState(
    val currentTab: MainTab,
    val mainTabs: List<MainTabItem>
)