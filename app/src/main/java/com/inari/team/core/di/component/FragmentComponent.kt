package com.inari.team.core.di.component

import com.inari.team.core.di.FragmentModule
import com.inari.team.core.di.scopes.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [FragmentModule::class])
interface FragmentComponent {


}