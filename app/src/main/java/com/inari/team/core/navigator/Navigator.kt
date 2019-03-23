package com.inari.team.core.navigator

import javax.inject.Singleton

@Singleton
interface Navigator {

    fun navigateToMainActivity()

    fun navigateToLogsActivity()

    fun navigateToGnssSettingsActivity()

    fun navigateToModesActivity()

}