package pro.devapp.walkietalkiek.ui

import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.factory.MainScreenInitStateFactory
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState

class MainViewMode(
    private val initStateFactory: MainScreenInitStateFactory,
    private val coroutineContextProvider: CoroutineContextProvider
): MviViewModel<MainScreenState, MainScreenAction, MainScreenEvent>(
    initState = initStateFactory.createInitState(),
    coroutineContextProvider = coroutineContextProvider.default
) {
}