package com.inari.team.core.navigator

import android.app.Activity
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.logs.LogsActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import org.jetbrains.anko.intentFor
import javax.inject.Inject

@PerActivity
class ActivityNavigator @Inject constructor(private var activity: Activity) : Navigator {

    override fun navigateToMainActivity() {
        with(activity) {
            startActivity(intentFor<MainActivity>())
        }
    }

    override fun navigateToLogsActivity() {
        with(activity) {
            startActivity(intentFor<LogsActivity>())
        }
    }

    override fun navigateToGnssSettingsActivity() {
        with(activity) {
            startActivity(intentFor<GnssSettingsActivity>())
        }
    }

    override fun navigateToModesActivity() {
        with(activity) {
            startActivity(intentFor<ModesActivity>())
        }
    }

}