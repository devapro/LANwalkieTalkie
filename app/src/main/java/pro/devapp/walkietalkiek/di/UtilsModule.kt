package pro.devapp.walkietalkiek.di

import dagger.Module
import dagger.Provides
import pro.devapp.walkietalkiek.utils.permission.UtilPermission
import javax.inject.Singleton

@Module
class UtilsModule {

    @Provides
    @Singleton
    fun utilPermission() = UtilPermission()
}