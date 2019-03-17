package com.inari.team.core.di

import android.app.Activity
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.core.navigator.ActivityNavigator
import com.inari.team.core.navigator.Navigator
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: Activity) {

    @Provides
    @PerActivity
    internal fun provideActivity(): Activity = this.activity

    @Provides
    @PerActivity
    internal fun provideNavigator(): Navigator = ActivityNavigator(activity)

}