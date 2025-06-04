package pro.devapp.walkietalkiek.factory

import pro.devapp.walkietalkiek.core.mvi.InitStateFactory
import pro.devapp.walkietalkiek.model.MainScreenState
import pro.devapp.walkietalkiek.model.MainTab

class MainScreenInitStateFactory(
    private val mainTabsFactory: MainTabsFactory
): InitStateFactory<MainScreenState> {
    override fun createInitState(): MainScreenState {
        return MainScreenState(
            currentTab = MainTab.PTT,
            mainTabs = mainTabsFactory.createTabs(),
            requiredPermissions = emptyList()
        )
    }
}