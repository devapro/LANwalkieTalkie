package pro.devapp.walkietalkiek.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import pro.devapp.walkietalkiek.ui.dialogs.messages.MessagesViewModel

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MessagesViewModel::class)
    internal abstract fun messagesViewModel(viewModel: MessagesViewModel): ViewModel
}