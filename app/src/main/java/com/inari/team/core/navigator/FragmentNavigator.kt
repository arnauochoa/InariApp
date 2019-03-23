package com.inari.team.core.navigator

import android.content.Intent
import android.support.v4.app.Fragment
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.presentation.ui.logs.LogsActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import javax.inject.Inject

@PerFragment
class FragmentNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

    override fun navigateToMainActivity() {
        with(fragment) {
            startActivity(Intent(context, MainActivity::class.java))
        }
    }

    override fun navigateToLogsActivity() {
        with(fragment) {
            startActivity(Intent(context, LogsActivity::class.java))
        }
    }

    override fun navigateToGnssSettingsActivity() {
        with(fragment) {
            startActivity(Intent(context, GnssSettingsActivity::class.java))
        }
    }

    override fun navigateToModesActivity() {
        with(fragment) {
            startActivity(Intent(context, ModesActivity::class.java))
        }
    }

}