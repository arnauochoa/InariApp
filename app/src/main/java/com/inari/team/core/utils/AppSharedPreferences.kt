package com.inari.team.core.utils

import android.content.SharedPreferences
import com.google.android.gms.maps.GoogleMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.core.App
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.ui.statistics.StatisticsFragment


class AppSharedPreferences {

    private val mPrefs: SharedPreferences = App.getAppContext().getSharedPreferences(MY_PREFS, 0)

    companion object {

        const val MY_PREFS: String = "MY_PREFS"
        const val MODES: String = "modes"
        const val COLORS: String = "colors"
        const val AVG_ENABLED: String = "avgenabled"
        const val AVGTIME: String = "avgtime"
        const val MASK: String = "mask"
        const val CN0_MASK: String = "cno_mask"
        const val TUTORIAL_SHOWN = "tutorial shown"
        const val SELECTED_MAP_TYPE = "selected_map_type"
        const val SELECTED_GRAPH_TYPE = "selected_graph_type"
        const val GNSS_LOGGING_ENABLED = "gnss_logging_enabled"


        private var INSTANCE: AppSharedPreferences? = null

        fun getInstance(): AppSharedPreferences {
            if (INSTANCE == null) INSTANCE = AppSharedPreferences()
            return INSTANCE!!
        }
    }

    fun isTutorialShown() = mPrefs.getBoolean(TUTORIAL_SHOWN, false)
    fun setTutorialShown() {
        mPrefs.edit()
            .putBoolean(TUTORIAL_SHOWN, true)
            .apply()
    }

    fun getSelectedMapType(): Int = mPrefs.getInt(SELECTED_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
    fun setSelectedMapType(type: Int) {
        mPrefs.edit()
            .putInt(SELECTED_MAP_TYPE, type)
            .apply()
    }

    fun getSelectedGraphType(): String =
        mPrefs.getString(SELECTED_GRAPH_TYPE, StatisticsFragment.GRAPH_AGC_CNO) ?: StatisticsFragment.GRAPH_AGC_CNO

    fun setSelectedGraphType(graphType: String) {
        mPrefs.edit()
            .putString(SELECTED_GRAPH_TYPE, graphType)
            .apply()
    }

    fun isGnssLoggingEnabled() = mPrefs.getBoolean(GNSS_LOGGING_ENABLED, false)
    fun setGnssLoggingEnabled(enabled: Boolean) {
        mPrefs.edit()
            .putBoolean(GNSS_LOGGING_ENABLED, enabled)
            .apply()
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

    fun getSelectedMask(): Int = mPrefs.getInt(MASK, 15)
    fun setSelectedMask(mask: Int) {
        mPrefs.edit()
            .putInt(MASK, mask)
            .apply()
    }

    fun getSelectedCnoMask(): Int = mPrefs.getInt(CN0_MASK, 0)
    fun setSelectedCnoMask(mask: Int) {
        mPrefs.edit()
            .putInt(CN0_MASK, mask)
            .apply()
    }

}