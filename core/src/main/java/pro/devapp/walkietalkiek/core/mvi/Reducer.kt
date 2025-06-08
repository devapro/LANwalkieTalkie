package pro.devapp.walkietalkiek.core.mvi

import kotlin.reflect.KClass

interface Reducer<ACTION : NEXT, STATE, NEXT : Any, EVENT> {

    val actionClass: KClass<ACTION>

    @Throws(ClassCastException::class)
    suspend fun reduce(action: ACTION, getState: () -> STATE): Result<STATE, NEXT, EVENT?>

    data class Result<out STATE, out NEXT_ACTION, out EVENT>(
        val state: STATE,
        val action: NEXT_ACTION? = null,
        val event: EVENT? = null
    )
}