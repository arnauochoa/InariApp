package com.inari.team.utils

import android.content.SharedPreferences
import com.inari.team.App

class AppSharedPreferences {

    private val mPrefs: SharedPreferences = App.getAppContext().getSharedPreferences(MY_PREFS, 0)

    companion object {

        const val MY_PREFS: String = "MY_PREFS"
        const val USER_TOKEN: String = "userToken"
        const val FIREBASE_TOKEN: String = "firebaseToken"

        private var INSTANCE: AppSharedPreferences? = null

        fun getInstance(): AppSharedPreferences {
            if (INSTANCE == null) INSTANCE = AppSharedPreferences()
            return INSTANCE!!
        }
    }

    fun logout() {
        mPrefs.edit()
            .remove(USER_TOKEN)
            .apply()
    }

    fun setUserToken(userToken: String) {
        mPrefs.edit()
            .putString(USER_TOKEN, userToken)
            .apply()
    }

    fun getUserToken(): String {
        return mPrefs.getString(USER_TOKEN, "")
    }

    fun setFirebaseTokenSent(sent: Boolean) {
        mPrefs.edit()
            .putBoolean(FIREBASE_TOKEN, sent)
            .apply()
    }

    fun isFirebaseTokenSent(): Boolean {
        return mPrefs.getBoolean(FIREBASE_TOKEN, false)
    }

}