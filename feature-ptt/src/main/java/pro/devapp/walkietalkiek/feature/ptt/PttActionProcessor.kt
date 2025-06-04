package pro.devapp.walkietalkiek.feature.ptt

import pro.devapp.walkietalkiek.core.mvi.ActionProcessor
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.feature.ptt.factory.PttInitStateFactory
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

internal class PttActionProcessor(
    private val initStateFactory: PttInitStateFactory,
    private val coroutineContextProvider: CoroutineContextProvider,
    reducers: Set<Reducer<PttAction, PttScreenState, PttAction, PttEvent>>,
): ActionProcessor<PttScreenState, PttAction, PttEvent>(
    reducers = reducers,
    initStateFactory = initStateFactory,
    coroutineDispatcher = coroutineContextProvider.default
)