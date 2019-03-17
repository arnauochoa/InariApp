package com.inari.team.core.base

import android.support.v4.app.Fragment
import com.inari.team.core.di.FragmentModule
import com.inari.team.core.utils.extensions.ViewModelFactory
import com.inari.team.core.utils.extensions.getAppComponent
import com.inari.team.core.di.component.FragmentComponent
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    val fragmentComponent: FragmentComponent by lazy {
        getAppComponent().plus(FragmentModule(this))
    }

}