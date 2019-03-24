package com.inari.team.core.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.R
import com.inari.team.core.App
import com.inari.team.presentation.model.Mode


class AppSharedPreferences {

    private val mPrefs: SharedPreferences = App.getAppContext().getSharedPreferences(MY_PREFS, 0)

    companion object {

        const val MY_PREFS: String = "MY_PREFS"
        const val MODES: String = "modes"
        const val PVT_INFO: String = "PVT_INFO"
        const val COLORS: String = "colors"
        const val AVG_ENABLED: String = "avgenabled"
        const val AVGTIME: String = "avgtime"
        const val MASK: String = "mask"


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

    fun getSelectedModesList(): List<Mode> {
        return getModesList().filter { it.isSelected }
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

    fun getData(type: String): String? {
        return mPrefs.getString(type, "")
    }

    fun isAverageEnabled(): Boolean = mPrefs.getBoolean(AVG_ENABLED, true)
    fun setAverageEnabled(enabled: Boolean) {
        mPrefs.edit()
            .putBoolean(AVG_ENABLED, enabled)
            .apply()
    }

    fun getAverage(): Int = mPrefs.getInt(AVGTIME, 5)
    fun setAverage(avg: Int) {
        mPrefs.edit()
            .putInt(AVGTIME, avg)
            .apply()
    }

    fun getSelectedMask(): Int = mPrefs.getInt(MASK, 10)
    fun setSelectedMask(mask: Int) {
        mPrefs.edit()
            .putInt(MASK, mask)
            .apply()
    }

    fun saveColors() {
        val colorList = arrayListOf<Int>()
        colorList.add(R.color.colorLegend1)
        colorList.add(R.color.colorLegend2)
        colorList.add(R.color.colorLegend3)
        colorList.add(R.color.colorLegend4)
        colorList.add(R.color.colorLegend5)

        val colorListGson = Gson().toJson(colorList)

        mPrefs.edit()
            .putString(COLORS, colorListGson)
            .apply()

    }

    fun getColor(): Int {
        val colorListGson = mPrefs.getString(COLORS, "") ?: ""

        val type = object : TypeToken<ArrayList<Int>>() {}.type

        var color = -1

        if (colorListGson.isNotBlank()) {
            val colorList = Gson().fromJson<ArrayList<Int>>(colorListGson, type) ?: arrayListOf()
            if (colorList.isNotEmpty()) {
                val obtainedColor = colorList[0]
                colorList.remove(obtainedColor)
                val colorListGsonChanged = Gson().toJson(colorList)
                mPrefs.edit()
                    .putString(COLORS, colorListGsonChanged)
                    .apply()

                color = obtainedColor
            }
        }

        return color

    }

    fun setColorToAvailableColorsList(color: Int) {
        if (color != -1) {
            val colorListGson = mPrefs.getString(COLORS, "") ?: ""

            val type = object : TypeToken<ArrayList<Int>>() {}.type

            if (colorListGson.isNotBlank()) {
                val colorList = Gson().fromJson<ArrayList<Int>>(colorListGson, type) ?: arrayListOf()
                colorList.add(color)
                val colorListGsonChanged = Gson().toJson(colorList)
                mPrefs.edit()
                    .putString(COLORS, colorListGsonChanged)
                    .apply()
            }
        }
    }

}