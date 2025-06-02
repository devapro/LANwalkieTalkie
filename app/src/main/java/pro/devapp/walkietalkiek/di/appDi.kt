package pro.devapp.walkietalkiek.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.factory.MainScreenInitStateFactory
import pro.devapp.walkietalkiek.factory.MainTabsFactory
import pro.devapp.walkietalkiek.reducers.ChangeScreenReducer
import pro.devapp.walkietalkiek.reducers.MainActionProcessor
import pro.devapp.walkietalkiek.ui.MainViewMode

val appModule: Module = module {
    coreDi()
    factoriesDi()
    viewModelsDi()
    reducersDi()
}

private fun Module.coreDi() {
    factoryOf(::CoroutineContextProvider)
}

private fun Module.factoriesDi() {
    factoryOf(::MainScreenInitStateFactory)
    factoryOf(::MainTabsFactory)
}

private fun Module.viewModelsDi() {
    viewModelOf(::MainViewMode)
}

private fun Module.reducersDi() {
    factoryOf(::ChangeScreenReducer)
    factory {
        MainActionProcessor(
            reducers = setOf(
                get(ChangeScreenReducer::class)
            ),
            initStateFactory = get(),
            coroutineContextProvider = get()
        )
    }
}