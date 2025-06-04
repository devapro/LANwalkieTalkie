package pro.devapp.walkietalkiek.feature.ptt.model

internal sealed interface PttAction {
    data object InitScreen : PttAction
}