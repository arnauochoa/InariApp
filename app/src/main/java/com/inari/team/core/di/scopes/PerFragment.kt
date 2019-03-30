package com.inari.team.core.di.scopes

import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME


@Scope
@Retention(RUNTIME)
annotation class PerFragment
