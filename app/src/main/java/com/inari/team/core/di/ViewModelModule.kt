package com.inari.team.core.di


import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.inari.team.core.utils.extensions.ViewModelFactory
import com.inari.team.core.utils.extensions.ViewModelKey
import com.inari.team.presentation.ui.position.PositionViewModel
import com.inari.team.presentation.ui.status.StatusViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    internal abstract fun statusViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PositionViewModel::class)
    internal abstract fun positionViewModel(viewModel: PositionViewModel): ViewModel

}
