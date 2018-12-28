package com.inari.team

import android.app.Application
import android.content.Context

class App : Application() {


    companion object {

        lateinit var INSTANCE: App

        fun getAppContext(): App = INSTANCE

        fun get(context: Context): App {
            return context.applicationContext as App
        }

    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

}