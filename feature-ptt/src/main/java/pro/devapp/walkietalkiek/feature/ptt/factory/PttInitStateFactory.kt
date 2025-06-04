package pro.devapp.walkietalkiek.feature.ptt.factory

import pro.devapp.walkietalkiek.core.mvi.InitStateFactory
import pro.devapp.walkietalkiek.feature.ptt.model.PttScreenState

internal class PttInitStateFactory: InitStateFactory<PttScreenState> {
    override fun createInitState(): PttScreenState {
        return PttScreenState(
            isConnected = false,
            myIP = "-",
        )
    }
}