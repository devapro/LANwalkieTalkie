package pro.devapp.walkietalkiek.core.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

abstract class ActionProcessor<STATE, ACTION : Any, EVENT>(
    private val reducers: Set<Reducer<ACTION, STATE, ACTION, EVENT>>,
    initStateFactory: InitStateFactory<STATE>,
    coroutineDispatcher: CoroutineDispatcher
) {

    private val _state = MutableStateFlow(initStateFactory.createInitState())
    val state
        get() = _state.asStateFlow()

    private val _event = Channel<EVENT>()
    val event: Flow<EVENT>
        get() = _event.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    // limit parallelism to achieve sequential UI updates when a socket event is received
    private val dispatcher = coroutineDispatcher.limitedParallelism(1)

    suspend fun process(action: ACTION) {
        return withContext(dispatcher) {
            val result = internalProcess(action)
            _state.update { result.state }
            result.event?.let { optionsEvent -> _event.send(optionsEvent) }
            result.action?.let { nextAction -> process(nextAction) }
        }
    }

    private suspend fun internalProcess(action: ACTION): Reducer.Result<STATE, ACTION?, EVENT?> {
        if (reducers.distinctBy { it.actionClass }.size != reducers.size) {
            throw Exception("Reducers must have unique action classes")
        }
        val result = try {
            reducers.firstOrNull { it.actionClass == action::class }?.reduce(action) { _state.value }
        } catch (e: ClassCastException) {
            // return the same state in case the expected state does not match the actual state
            Reducer.Result(_state.value)
        }
        return result ?: throw Exception("Reducer for action ${action::class} not found")
    }
}