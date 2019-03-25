package com.inari.team.core.di.component

import com.inari.team.core.di.ActivityModule
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import com.inari.team.presentation.ui.splash.SplashActivity
import com.inari.team.presentation.ui.statisticsdetail.StatisticsDetailActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(splashActivity: SplashActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(gnssGnssSettingsActivity: GnssSettingsActivity)

    fun inject(modesActivity: ModesActivity)

    fun inject(statisticsDetailActivity: StatisticsDetailActivity)
}