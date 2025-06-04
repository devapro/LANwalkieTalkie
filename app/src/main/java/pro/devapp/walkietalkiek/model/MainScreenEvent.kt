package pro.devapp.walkietalkiek.model

sealed interface MainScreenEvent {
    data class RequestPermissions(
        val permissions: List<String>
    ) : MainScreenEvent
}