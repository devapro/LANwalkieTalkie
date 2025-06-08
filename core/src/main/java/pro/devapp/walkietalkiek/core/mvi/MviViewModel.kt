package pro.devapp.walkietalkiek.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class MviViewModel<STATE, ACTION : Any, EVENT>(
    private val actionProcessor: ActionProcessor<STATE, ACTION, EVENT>
) : ViewModel() {

    val state
        get() = actionProcessor.state

    val event
        get() = actionProcessor.event

    fun onAction(action: ACTION) {
        viewModelScope.launch {
            actionProcessor.process(action)
        }
    }
}