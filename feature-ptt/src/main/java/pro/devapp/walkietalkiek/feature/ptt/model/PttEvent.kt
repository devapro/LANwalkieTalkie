package pro.devapp.walkietalkiek.feature.ptt.model

internal sealed interface PttEvent {
    data object InitScreen : PttEvent
}