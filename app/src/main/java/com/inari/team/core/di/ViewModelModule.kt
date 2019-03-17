package com.inari.team.core.di


import android.arch.lifecycle.ViewModelProvider
import com.inari.team.core.utils.extensions.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @ViewModelKey(MainViewModel::class)
//    internal abstract fun mainViewModel(viewModel: MainViewModel): ViewModel
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(RestaurantsViewModel::class)
//    internal abstract fun restaurantsViewModel(viewModel: RestaurantsViewModel): ViewModel
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(RestaurantDetailViewModel::class)
//    internal abstract fun restaurantDetailViewModel(viewModel: RestaurantDetailViewModel): ViewModel


}
