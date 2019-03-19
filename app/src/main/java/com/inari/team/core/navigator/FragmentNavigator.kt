package com.inari.team.core.navigator

import android.content.Intent
import android.support.v4.app.Fragment
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.presentation.ui.logs.LogsActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.modes.ModesActivity.Companion.COMPARING_EXTRA
import com.inari.team.presentation.ui.position.PositionFragment
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
            startActivityForResult(
                Intent(context, GnssSettingsActivity::class.java),
                PositionFragment.SETTINGS_RESULT_CODE
            )
        }
    }

    override fun navigateToModesActivity(isComparing: Boolean) {
        with(fragment) {
            val i = Intent(context, ModesActivity::class.java)
            i.putExtra(COMPARING_EXTRA, isComparing)
            startActivity(i)
        }
    }


}