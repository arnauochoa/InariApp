package com.inari.team.core.di.component

import com.inari.team.core.di.ActivityModule
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.MainActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(mainActivity: MainActivity)

    fun inject(gnssGnssSettingsActivity: GnssSettingsActivity)

}