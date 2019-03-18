package com.inari.team.core.base

import android.arch.lifecycle.ViewModelProvider
import android.support.v7.app.AppCompatActivity
import com.inari.team.core.di.ActivityModule
import com.inari.team.core.utils.extensions.getAppComponent
import com.inari.team.core.di.component.ActivityComponent
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val activityComponent: ActivityComponent by lazy {
        getAppComponent().plus(ActivityModule(this))
    }

}
