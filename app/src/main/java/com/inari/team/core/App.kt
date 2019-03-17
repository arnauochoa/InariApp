package com.inari.team.core

import android.content.Context
import android.support.multidex.MultiDexApplication
import com.inari.team.BuildConfig
import com.inari.team.R
import com.inari.team.core.di.AppModule
import com.inari.team.core.di.component.AppComponent
import timber.log.Timber

class App : MultiDexApplication() {

    lateinit var appComponent: AppComponent private set

    companion object {

        lateinit var INSTANCE: App

        fun getAppContext(): App = INSTANCE

        fun get(context: Context): App {
            return context.applicationContext as App
        }

    }

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
        INSTANCE = this
        initDagger()
        initTimber()
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}