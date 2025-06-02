package pro.devapp.walkietalkiek.factory

import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.model.MainTab
import pro.devapp.walkietalkiek.model.MainTabItem

class MainTabsFactory {

    fun createTabs(): List<MainTabItem> {
        return listOf(
            MainTabItem(
                id = "ptt",
                title = "PTT",
                icon = R.drawable.ptt,
                screen = MainTab.PTT
            ),
            MainTabItem(
                id = "chat",
                title = "Chat",
                icon = R.drawable.chat,
                screen = MainTab.CHAT
            ),
            MainTabItem(
                id = "settings",
                title = "Settings",
                icon = R.drawable.settings,
                screen = MainTab.SETTINGS
            ),
        )
    }
}