package com.inari.team.core.navigator

import android.content.Intent
import android.support.v4.app.Fragment
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.ui.MainActivity
import com.inari.team.ui.logs.LogsActivity
import org.jetbrains.anko.intentFor
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

}