package pro.devapp.walkietalkiek.model

sealed interface MainScreenEvent {
    data class ShowToast(val message: String) : MainScreenEvent
    data class ShowSnackbar(val message: String) : MainScreenEvent
    data class ShowDialog(val message: String) : MainScreenEvent
}