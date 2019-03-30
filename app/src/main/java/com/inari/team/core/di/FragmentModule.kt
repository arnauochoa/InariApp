package com.inari.team.core.di

import android.support.v4.app.Fragment
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.core.navigator.FragmentNavigator
import com.inari.team.core.navigator.Navigator
import dagger.Module
import dagger.Provides

@Module
class FragmentModule(private val fragment: Fragment) {

    @Provides
    @PerFragment
    fun provideFragment(): Fragment = this.fragment

    @Provides
    @PerFragment
    internal fun provideNavigator(): Navigator = FragmentNavigator(fragment)

}