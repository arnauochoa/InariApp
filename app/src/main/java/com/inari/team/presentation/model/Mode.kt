package com.inari.team.presentation.model

import com.inari.team.presentation.model.PositionParameters.ALG_LS
import com.inari.team.presentation.model.PositionParameters.ALG_WLS
import com.inari.team.presentation.model.PositionParameters.BAND_L1
import com.inari.team.presentation.model.PositionParameters.BAND_L5
import com.inari.team.presentation.model.PositionParameters.CONST_GAL
import com.inari.team.presentation.model.PositionParameters.CONST_GPS
import com.inari.team.presentation.model.PositionParameters.CORR_IONOFREE
import com.inari.team.presentation.model.PositionParameters.CORR_IONOSPHERE
import com.inari.team.presentation.model.PositionParameters.CORR_TROPOSPHERE
import org.json.JSONObject

data class Mode(
    var id: Int,
    val name: String = "",
    val constellations: ArrayList<Int>,
    val bands: ArrayList<Int>,
    val corrections: ArrayList<Int>,
    val algorithm: Int,
    val avgTime: Long = PositionParameters.AVERAGING_TIME_SEC_1, //s
    var isSelected: Boolean,
    var color: Int = -1
) {


    fun toJSONObject(): JSONObject {
        val modeJson = JSONObject()
        modeJson.put(PositionParameters.CONSTELLATIONS_KEY, getConstellationsJson())
        modeJson.put(PositionParameters.BANDS_KEY, getBandsJson())
        modeJson.put(PositionParameters.CORRECTIONS_KEY, getCorrectionsJson())
        modeJson.put(PositionParameters.ALGORITHM_KEY, getAlgorithmJson())
        return modeJson
    }

    private fun getConstellationsJson(): JSONObject {
        val constellationsJson = JSONObject()

        if (this.constellations.contains(PositionParameters.CONST_GPS)) {
            constellationsJson.put(PositionParameters.CONST_GPS_STR, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GPS_STR, false)
        }
        if (this.constellations.contains(PositionParameters.CONST_GAL)) {
            constellationsJson.put(PositionParameters.CONST_GAL_STR, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GAL_STR, false)
        }
        if (this.constellations.contains(PositionParameters.CONST_GLO)) {
            constellationsJson.put(PositionParameters.CONST_GLO_STR, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GLO_STR, false)
        }

        return constellationsJson
    }

    private fun getBandsJson(): JSONObject {
        val bandsJson = JSONObject()

        if (this.bands.contains(PositionParameters.BAND_L1)) {
            bandsJson.put(PositionParameters.BAND_L1_STR, true)
        } else {
            bandsJson.put(PositionParameters.BAND_L1_STR, false)
        }
        if (this.bands.contains(PositionParameters.BAND_L5)) {
            bandsJson.put(PositionParameters.BAND_L5_STR, true)
        } else {
            bandsJson.put(PositionParameters.BAND_L5_STR, false)
        }

        return bandsJson
    }

    private fun getCorrectionsJson(): JSONObject {
        val correctionsJson = JSONObject()

        if (this.corrections.contains(PositionParameters.CORR_IONOSPHERE)) {
            correctionsJson.put(PositionParameters.CORR_IONOSPHERE_STR, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_IONOSPHERE_STR, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_TROPOSPHERE)) {
            correctionsJson.put(PositionParameters.CORR_TROPOSPHERE_STR, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_TROPOSPHERE_STR, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_IONOFREE)) {
            correctionsJson.put(PositionParameters.CORR_IONOFREE_STR, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_IONOFREE_STR, false)
        }


        return correctionsJson
    }

    private fun getAlgorithmJson(): JSONObject {
        val algorithmJson = JSONObject()
        algorithmJson.put(PositionParameters.ALG_LS_STR, false)
        algorithmJson.put(PositionParameters.ALG_WLS_STR, false)
        algorithmJson.put(PositionParameters.ALG_KALMAN_STR, false)

        when (this.algorithm) {
            PositionParameters.ALG_LS -> algorithmJson.put(PositionParameters.ALG_LS_STR, true)
            PositionParameters.ALG_WLS -> algorithmJson.put(PositionParameters.ALG_WLS_STR, true)
            else -> algorithmJson.put(PositionParameters.ALG_LS_STR, true) // default value = LS
        }

        return algorithmJson
    }


    /**
     * STRING GETTERS
     */

    fun constellationsAsString(): CharSequence? {
        var constellationsString = ""

        if (constellations.contains(CONST_GPS)) {
            constellationsString = "GPS"
        }
        if (constellations.contains(CONST_GAL)) {
            if (constellationsString.isNotBlank()) constellationsString += ", "
            constellationsString += "Galileo"
        }
        return constellationsString
    }

    fun bandsAsString(): CharSequence? {
        var bandsString = ""

        if (bands.contains(BAND_L1)) {
            bandsString = "L1"
        }
        if (bands.contains(BAND_L5)) {
            if (bandsString.isNotBlank()) bandsString += ", "
            bandsString += "L5"
        }
        return bandsString
    }

    fun correctionsAsString(): CharSequence? {
        var correctionsString = ""

        if (corrections.contains(CORR_IONOSPHERE)) {
            correctionsString = "Ionosphere"
        }
        if (corrections.contains(CORR_TROPOSPHERE)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Troposphere"
        }
        if (corrections.contains(CORR_IONOFREE)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Iono-free"
        }
        if (correctionsString.isBlank()) correctionsString = "None"
        return correctionsString
    }

    fun algorithmAsString(): CharSequence? {
        var algorithmString = ""

        when (algorithm) {
            ALG_LS -> algorithmString = "Least Squares"
            ALG_WLS -> algorithmString = "Weighted Least Squares"
        }
        return algorithmString
    }

}
