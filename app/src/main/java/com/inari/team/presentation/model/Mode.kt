package com.inari.team.presentation.model

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

    companion object {

        const val CONST_GPS: Int = 1
        const val CONST_GAL: Int = 2
        const val CONST_GLO: Int = 3

        const val BAND_L1: Int = 1
        const val BAND_L5: Int = 2

        const val CORR_IONOSPHERE: Int = 1
        const val CORR_TROPOSPHERE: Int = 2
        const val CORR_IONOFREE: Int = 3

        const val ALG_LS: Int = 1
        const val ALG_WLS: Int = 2
    }


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

        if (this.constellations.contains(Mode.CONST_GPS)) {
            constellationsJson.put(PositionParameters.CONST_GPS, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GPS, false)
        }
        if (this.constellations.contains(Mode.CONST_GAL)) {
            constellationsJson.put(PositionParameters.CONST_GAL, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GAL, false)
        }
        if (this.constellations.contains(Mode.CONST_GLO)) {
            constellationsJson.put(PositionParameters.CONST_GLO, true)
        } else {
            constellationsJson.put(PositionParameters.CONST_GLO, false)
        }

        return constellationsJson
    }

    private fun getBandsJson(): JSONObject {
        val bandsJson = JSONObject()

        if (this.bands.contains(Mode.BAND_L1)) {
            bandsJson.put(PositionParameters.BAND_L1, true)
        } else {
            bandsJson.put(PositionParameters.BAND_L1, false)
        }
        if (this.bands.contains(Mode.BAND_L5)) {
            bandsJson.put(PositionParameters.BAND_L5, true)
        } else {
            bandsJson.put(PositionParameters.BAND_L5, false)
        }

        return bandsJson
    }

    private fun getCorrectionsJson(): JSONObject {
        val correctionsJson = JSONObject()

        if (this.corrections.contains(Mode.CORR_IONOSPHERE)) {
            correctionsJson.put(PositionParameters.CORR_IONOSPHERE, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_IONOSPHERE, false)
        }
        if (this.corrections.contains(Mode.CORR_TROPOSPHERE)) {
            correctionsJson.put(PositionParameters.CORR_TROPOSPHERE, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_TROPOSPHERE, false)
        }
        if (this.corrections.contains(Mode.CORR_IONOFREE)) {
            correctionsJson.put(PositionParameters.CORR_IONOFREE, true)
        } else {
            correctionsJson.put(PositionParameters.CORR_IONOFREE, false)
        }


        return correctionsJson
    }

    private fun getAlgorithmJson(): JSONObject {
        val algorithmJson = JSONObject()
        algorithmJson.put(PositionParameters.ALG_LS, false)
        algorithmJson.put(PositionParameters.ALG_WLS, false)
        algorithmJson.put(PositionParameters.ALG_KALMAN, false)

        when (this.algorithm) {
            Mode.ALG_LS -> algorithmJson.put(PositionParameters.ALG_LS, true)
            Mode.ALG_WLS -> algorithmJson.put(PositionParameters.ALG_WLS, true)
            else -> algorithmJson.put(PositionParameters.ALG_LS, true) // default value = LS
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
