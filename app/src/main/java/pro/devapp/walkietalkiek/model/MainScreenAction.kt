package pro.devapp.walkietalkiek.model

sealed interface MainScreenAction {
    data class ChangeScreen(val screen: MainTab) : MainScreenAction
}