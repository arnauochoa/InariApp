package com.inari.team.presentation.ui.position

import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.AppSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PositionViewModel @Inject constructor(private val mPrefs: AppSharedPreferences) : BaseViewModel()
