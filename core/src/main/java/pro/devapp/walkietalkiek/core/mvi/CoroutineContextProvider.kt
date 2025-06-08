package pro.devapp.walkietalkiek.core.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

open class CoroutineContextProvider {

    open val main: CoroutineDispatcher by lazy { Dispatchers.Main }

    open val io: CoroutineDispatcher by lazy { Dispatchers.IO }

    open val database: CoroutineDispatcher by lazy { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }

    open val default: CoroutineDispatcher by lazy { Dispatchers.Default }

    val globalScope: CoroutineScope by lazy { CoroutineScope(SupervisorJob(null) + main) }

    open fun createScope(
        context: CoroutineContext
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob(null) + context)
    }
}