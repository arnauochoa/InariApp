package com.inari.team.core.navigator

import javax.inject.Singleton

@Singleton
interface Navigator {

    fun navigateToTutorialActivtiy()

    fun navigateToMainActivity()

    fun navigateToGnssSettingsActivity()

    fun navigateToModesActivity()

}