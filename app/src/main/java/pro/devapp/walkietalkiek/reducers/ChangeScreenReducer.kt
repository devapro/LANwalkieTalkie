package pro.devapp.walkietalkiek.reducers

import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState

class ChangeScreenReducer: Reducer<MainScreenAction.ChangeScreen, MainScreenState, MainScreenAction, MainScreenEvent> {

    override val actionClass = MainScreenAction.ChangeScreen::class

    override suspend fun reduce(
        action: MainScreenAction.ChangeScreen,
        getState: () -> MainScreenState
    ): Reducer.Result<MainScreenState, MainScreenAction, MainScreenEvent?> {
        return Reducer.Result(
            state = getState().copy(
                currentTab = action.screen
            ),
            event = null
        )
    }

}