package pro.devapp.walkietalkiek.reducers

import pro.devapp.walkietalkiek.core.mvi.ActionProcessor
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.factory.MainScreenInitStateFactory
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState

class MainActionProcessor(
    private val initStateFactory: MainScreenInitStateFactory,
    private val coroutineContextProvider: CoroutineContextProvider,
    reducers: Set<Reducer<MainScreenAction, MainScreenState, MainScreenAction, MainScreenEvent>>,
): ActionProcessor<MainScreenState, MainScreenAction, MainScreenEvent>(
    reducers = reducers,
    initStateFactory = initStateFactory,
    coroutineDispatcher = coroutineContextProvider.default
)