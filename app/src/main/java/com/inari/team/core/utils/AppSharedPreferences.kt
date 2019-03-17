package com.inari.team.core.utils

import android.content.SharedPreferences
import com.inari.team.core.App
import com.inari.team.presentation.model.User
import com.google.gson.Gson
import javax.inject.Singleton

@Singleton
class AppSharedPreferences {

    private val mPrefs: SharedPreferences = App.getAppContext().getSharedPreferences(MY_PREFS, 0)

    companion object {

        const val MY_PREFS: String = "MY_PREFS"
        const val TEST_PREFS: String = "test"
        const val USER: String = "user"

        private var INSTANCE: AppSharedPreferences? = null

        fun getInstance(): AppSharedPreferences {
            if (INSTANCE == null) INSTANCE = AppSharedPreferences()
            return INSTANCE!!
        }
    }

    fun setTestPreference(test: String) {
        mPrefs.edit()
            .putString(TEST_PREFS, test)
            .apply()
    }

    fun getTestPreference(): String {
        return mPrefs.getString(TEST_PREFS, "") ?: ""
    }

    fun setUser(user: User) {

        val gson = Gson().toJson(user)

        mPrefs.edit()
            .putString(USER, gson)
            .apply()
    }

    fun getUser(): User? {
        val user = mPrefs.getString(USER, "")
        return Gson().fromJson<User>(user, User::class.java)
    }

    fun logOutUser() {
        mPrefs.edit()
            .remove(USER)
            .apply()
    }

}