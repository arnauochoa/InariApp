package com.inari.team.core.di.component

import com.inari.team.core.di.FragmentModule
import com.inari.team.core.di.scopes.PerFragment
import com.inari.team.presentation.ui.admin.AdminFragment
import com.inari.team.presentation.ui.restaurants.RestaurantsFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(restaurantsFragment: RestaurantsFragment)
    fun inject(adminFragment: AdminFragment)

}