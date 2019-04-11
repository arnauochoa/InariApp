package com.inari.team.core.navigator

import android.app.Activity
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.main.MainActivity.Companion.TUTORIAL_CODE
import com.inari.team.presentation.ui.maplog.MapLogActivity
import com.inari.team.presentation.ui.maplog.MapLogActivity.Companion.POSITIONS_EXTRA
import com.inari.team.presentation.ui.modes.ModesActivity
import com.inari.team.presentation.ui.settings.GnssSettingsActivity
import com.inari.team.presentation.ui.tutorial.TutorialActivity
import org.jetbrains.anko.intentFor
import javax.inject.Inject

@PerActivity
class ActivityNavigator @Inject constructor(private var activity: Activity) : Navigator {

    override fun navigateToTutorialActivtiy() {
        with(activity) {
            startActivityForResult(intentFor<TutorialActivity>(), TUTORIAL_CODE)
        }
    }

    override fun navigateToMainActivity() {
        with(activity) {
            startActivity(intentFor<MainActivity>())
        }
    }

    override fun navigateToGnssSettingsActivity() {
        with(activity) {
            startActivity(intentFor<GnssSettingsActivity>())
        }
    }

    override fun navigateToModesActivity() {
        with(activity) {
            startActivityForResult(intentFor<ModesActivity>(), MainActivity.SETTINGS_CODE)
        }
    }

    override fun navigateToMapLogActivity(positions: String) {
        with(activity) {
            startActivity(
                intentFor<MapLogActivity>(
                    POSITIONS_EXTRA to positions
                )
            )
        }
    }

}