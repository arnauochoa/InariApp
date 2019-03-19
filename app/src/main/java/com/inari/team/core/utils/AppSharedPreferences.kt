package com.inari.team.core.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.core.App
import com.inari.team.presentation.model.Mode


class AppSharedPreferences {

    private val mPrefs: SharedPreferences = App.getAppContext().getSharedPreferences(MY_PREFS, 0)

    companion object {

        const val MY_PREFS: String = "MY_PREFS"
        const val MODES: String = "modes"
        const val PVT_INFO: String = "PVT_INFO"


        private var INSTANCE: AppSharedPreferences? = null

        fun getInstance(): AppSharedPreferences {
            if (INSTANCE == null) INSTANCE = AppSharedPreferences()
            return INSTANCE!!
        }
    }

    fun getModesList(): ArrayList<Mode> {
        val gson = Gson()
        val type = object : TypeToken<List<Mode>>() {}.type

        val json = mPrefs.getString(MODES, "")

        return json?.let {
            if (it.isNotEmpty()) gson.fromJson<ArrayList<Mode>>(json, type)
            else arrayListOf()
        } ?: kotlin.run {
            arrayListOf<Mode>()
        }
    }

    fun getModesNames(): ArrayList<String> {
        val gson = Gson()
        val type = object : TypeToken<List<Mode>>() {}.type

        val json = mPrefs.getString(MODES, "")

        val modes = json?.let {
            if (it.isNotEmpty()) gson.fromJson<ArrayList<Mode>>(json, type)
            else arrayListOf()
        } ?: kotlin.run {
            arrayListOf<Mode>()
        }

        val modeNames = arrayListOf<String>()
        modes.forEach { mode ->
            modeNames.add(mode.name)
        }

        return modeNames
    }

    fun saveMode(mode: Mode) {
        val gson = Gson()
        val modesList = getModesList()
        modesList.add(mode)

        val json = gson.toJson(modesList)
        mPrefs.edit()
            .putString(MODES, json)
            .apply()
    }

    fun saveModes(modes: List<Mode>) {
        val gson = Gson()
        val json = gson.toJson(modes)
        mPrefs.edit()
            .putString(MODES, json)
            .apply()
    }

    fun deleteMode(mode: Mode): ArrayList<Mode> {
        val gson = Gson()
        val modesList = getModesList()
        modesList.remove(mode)

        val json = gson.toJson(modesList)
        mPrefs.edit()
            .remove(MODES)
            .apply()

        mPrefs.edit()
            .putString(MODES, json)
            .apply()

        return modesList
    }

    fun saveData(type: String, data: String) {
        mPrefs.edit()
            .putString(type, data)
            .apply()
    }

    fun deleteData(type: String) {
        mPrefs.edit()
            .remove(type)
            .apply()
    }

    fun getData(type: String): String? {
        return mPrefs.getString(type, "")
    }


}