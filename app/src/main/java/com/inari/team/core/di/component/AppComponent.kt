package com.inari.team.core.di.component

import com.inari.team.core.di.ActivityModule
import com.inari.team.core.di.AppModule
import com.inari.team.core.di.FragmentModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    operator fun plus(activityModule: ActivityModule): ActivityComponent
    operator fun plus(fragmentModule: FragmentModule): FragmentComponent

}
