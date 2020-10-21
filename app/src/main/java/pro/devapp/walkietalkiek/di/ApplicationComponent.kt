package pro.devapp.walkietalkiek.di

import android.app.Application
import androidx.fragment.app.DialogFragment
import dagger.Component
import dagger.android.AndroidInjectionModule
import pro.devapp.walkietalkiek.WalkieService
import pro.devapp.walkietalkiek.ui.MainActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        StorageModule::class,
        ApplicationModule::class,
        ViewModelModule::class,
        UtilsModule::class
    ]
)
interface ApplicationComponent {

    fun inject(application: Application)

//    @Component.Builder
//    interface Builder {
//
//        fun build(): ApplicationComponent
//
//        @BindsInstance
//        fun applicationBind(application: Application): Builder
//
//    }

    fun inject(fragment: DialogFragment)
    fun inject(service: WalkieService)
    fun inject(activity: MainActivity)
}