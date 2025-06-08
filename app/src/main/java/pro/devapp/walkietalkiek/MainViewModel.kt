package pro.devapp.walkietalkiek

import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState
import pro.devapp.walkietalkiek.reducers.MainActionProcessor

class MainViewMode(
    private val actionProcessor: MainActionProcessor
): MviViewModel<MainScreenState, MainScreenAction, MainScreenEvent>(
    actionProcessor = actionProcessor
)