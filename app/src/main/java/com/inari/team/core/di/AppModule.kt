package com.inari.team.core.di

import com.inari.team.core.App
import com.inari.team.core.utils.AppSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [PresentationModule::class, DataModule::class])
class AppModule(val app: App) {

    @Provides
    @Singleton
    fun provideApp(): App = app

    @Provides
    @Singleton
    internal fun provideSharedPreferences(): AppSharedPreferences =
        AppSharedPreferences.getInstance()
}
