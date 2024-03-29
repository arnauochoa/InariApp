package com.inari.team.core.di.component

import com.inari.team.core.di.FragmentModule
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.presentation.ui.about.AboutFragment
import com.inari.team.presentation.ui.logs.LogsFragment
import com.inari.team.presentation.ui.logs.MeasurementsLogsFragment
import com.inari.team.presentation.ui.logs.PositionLogsFragment
import com.inari.team.presentation.ui.position.PositionFragment
import com.inari.team.presentation.ui.statistics.StatisticsFragment
import com.inari.team.presentation.ui.status.StatusFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(positionFragment: PositionFragment)
    fun inject(statusFragment: StatusFragment)
    fun inject(statisticsFragment: StatisticsFragment)
    fun inject(logsFragment: LogsFragment)
    fun inject(aboutFragment: AboutFragment)
    fun inject(positionLogsFragment: PositionLogsFragment)
    fun inject(measurementsLogsFragment: MeasurementsLogsFragment)

}