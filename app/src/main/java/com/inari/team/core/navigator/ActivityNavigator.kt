package com.inari.team.core.navigator

import android.app.Activity
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.ui.MainActivity
import com.inari.team.ui.logs.LogsActivity
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
        with(activity){
            startActivity(intentFor<LogsActivity>())
        }
    }

}