package com.inari.team.core.di.component

import com.inari.team.core.di.ActivityModule
import com.inari.team.core.di.scopes.PerActivity
import com.inari.team.presentation.ui.login.LoginActivity
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.restaurantdetail.RestaurantDetailActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(mainActivity: MainActivity)
    fun inject(restaurantDetailActivity: RestaurantDetailActivity)
    fun inject(loginActivity: LoginActivity)

}