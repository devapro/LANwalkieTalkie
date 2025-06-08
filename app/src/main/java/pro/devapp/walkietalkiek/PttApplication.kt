package pro.devapp.walkietalkiek

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pro.devapp.walkietalkiek.di.appModule
import timber.log.Timber

class PttApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            allowOverride(true)
            androidContext(applicationContext)
            modules(
                appModule
            )
            // Uncomment to add koin logs
            // androidLogger(Level.DEBUG)
        }

        Timber.plant(
            Timber.DebugTree()
        )
    }
}