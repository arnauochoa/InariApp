package com.inari.team.core.di

import dagger.Module

@Module(includes = [RepositoryModule::class, ApiModule::class, DatabaseModule::class, FirebaseModule::class])
class DataModule
