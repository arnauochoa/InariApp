package com.inari.team.core.di.component

import com.inari.team.core.di.ActivityModule
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.ui.MainActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(mainActivity: MainActivity)

}