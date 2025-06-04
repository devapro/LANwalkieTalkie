package pro.devapp.walkietalkiek.reducers

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState

class InitAppReducer: Reducer<MainScreenAction.InitApp, MainScreenState, MainScreenAction, MainScreenEvent> {

    override val actionClass = MainScreenAction.InitApp::class

    override suspend fun reduce(
        action: MainScreenAction.InitApp,
        getState: () -> MainScreenState
    ): Reducer.Result<MainScreenState, MainScreenAction, MainScreenEvent?> {
        return Reducer.Result(
            state = getState(),
            action = MainScreenAction.CheckPermissions,
            event = null
        )
    }

}