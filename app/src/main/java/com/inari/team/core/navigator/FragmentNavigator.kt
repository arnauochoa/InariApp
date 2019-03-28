package com.inari.team.core.navigator

import android.content.Intent
import android.support.v4.app.Fragment
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.main.MainActivity.Companion.TUTORIAL_CODE
import com.inari.team.presentation.ui.maplog.MapLogActivity
import com.inari.team.presentation.ui.maplog.MapLogActivity.Companion.POSITIONS_EXTRA
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import com.inari.team.presentation.ui.tutorial.TutorialActivity
import javax.inject.Inject

@PerFragment
class FragmentNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

    override fun navigateToTutorialActivtiy() {
        with(fragment) {
            startActivityForResult(Intent(context, TutorialActivity::class.java), TUTORIAL_CODE)
        }
    }

    override fun navigateToMainActivity() {
        with(fragment) {
            startActivity(Intent(context, MainActivity::class.java))
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

    override fun navigateToMapLogActivity(positions: String) {
        with(fragment) {
            val i = Intent(context, MapLogActivity::class.java)
            i.putExtra(POSITIONS_EXTRA, positions)
            startActivity(i)
        }
    }

}