package com.inari.team.core.navigator

import android.app.Activity
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.MainActivity
import com.inari.team.presentation.ui.position.PositionFragment
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


    override fun navigateToGnssSettingsActivity() {
        with(activity) {
            startActivityForResult(intentFor<GnssSettingsActivity>(), PositionFragment.SETTINGS_RESULT_CODE)
        }
    }

}