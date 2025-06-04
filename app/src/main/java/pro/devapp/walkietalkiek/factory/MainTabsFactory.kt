package pro.devapp.walkietalkiek.factory

import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.model.MainTab
import pro.devapp.walkietalkiek.model.MainTabItem

class MainTabsFactory {

    fun createTabs(): List<MainTabItem> {
        return listOf(
            MainTabItem(
                title = "PTT",
                icon = R.drawable.ptt,
                screen = MainTab.PTT
            ),
            MainTabItem(
                title = "Chat",
                icon = R.drawable.chat,
                screen = MainTab.CHAT
            ),
            MainTabItem(
                title = "Settings",
                icon = R.drawable.settings,
                screen = MainTab.SETTINGS
            ),
            MainTabItem(
                title = "Exit",
                icon = R.drawable.power_off,
                screen = MainTab.OFF
            ),
        )
    }
}