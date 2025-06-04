package pro.devapp.walkietalkiek.feature.ptt

import pro.devapp.walkietalkiek.core.mvi.MviViewModel
import pro.devapp.walkietalkiek.feature.ptt.model.PttAction
import pro.devapp.walkietalkiek.feature.ptt.model.PttEvent
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

internal class PttViewModel(
    actionProcessor: PttActionProcessor
): MviViewModel<PttScreenState, PttAction, PttEvent>(
    actionProcessor = actionProcessor
)