package pro.devapp.walkietalkiek.model

sealed interface MainScreenAction {
    data object InitApp : MainScreenAction
    data object CheckPermissions : MainScreenAction
    data class ChangeScreen(val screen: MainTab) : MainScreenAction
}