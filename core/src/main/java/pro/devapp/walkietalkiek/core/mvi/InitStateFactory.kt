package pro.devapp.walkietalkiek.core.mvi

interface InitStateFactory<STATE> {

    fun createInitState(): STATE
}