package com.inari.team.data

import org.json.JSONObject

data class PositionParameters(
    val constellations: ArrayList<String>,
    val bands: ArrayList<String>,
    val corrections: ArrayList<String>,
    val algorithm: String?
) {
    companion object {

        const val CONSTELLATIONS_KEY = "constellations"
        const val CONST_GPS: String = "GPS"
        const val CONST_GAL: String = "Galileo"
        const val CONST_GLO: String = "GLONASS"

        const val BANDS_KEY = "bands"
        const val BAND_L1 = "L1"
        const val BAND_L5 = "L5"

        const val CORRECTIONS_KEY = "corrections"
        const val CORR_IONOSPHERE = "ionosphere"
        const val CORR_TROPOSPHERE = "troposphere"
        const val CORR_MULTIPATH = "multipath"
        const val CORR_PPP = "PPP"
        const val CORR_CAMERA = "camera"

        const val ALGORITHM_KEY = "algorithm"
        const val ALG_LS = "LS"
        const val ALG_WLS = "WLS"
        const val ALG_KALMAN = "KAL"

        const val AVERAGING_TIME_SEC_1 = 5L
        const val AVERAGING_TIME_SEC_2 = 10L
        const val AVERAGING_TIME_SEC_3 = 20L
    }

    fun toJSONObject(): JSONObject {
        val parametersJson = JSONObject()
        parametersJson.put(CONSTELLATIONS_KEY, getConstellationsJson())
        parametersJson.put(BANDS_KEY, getBandsJson())
        parametersJson.put(CORRECTIONS_KEY, getCorrectionsJson())
        parametersJson.put(ALGORITHM_KEY, getAlgorithmJson())
        return parametersJson
    }

    private fun getConstellationsJson(): JSONObject {
        val constellationsJson = JSONObject()

        if (this.constellations.contains(PositionParameters.CONST_GPS)) {
            constellationsJson.put(CONST_GPS, true)
        } else {
            constellationsJson.put(CONST_GPS, false)
        }
        if (this.constellations.contains(PositionParameters.CONST_GAL)) {
            constellationsJson.put(CONST_GAL, true)
        } else {
            constellationsJson.put(CONST_GAL, false)
        }
        if (this.constellations.contains(PositionParameters.CONST_GLO)) {
            constellationsJson.put(CONST_GLO, true)
        } else {
            constellationsJson.put(CONST_GLO, false)
        }

        return constellationsJson
    }

    private fun getBandsJson(): JSONObject {
        val bandsJson = JSONObject()

        if (this.bands.contains(PositionParameters.BAND_L1)) {
            bandsJson.put(BAND_L1, true)
        } else {
            bandsJson.put(BAND_L1, false)
        }
        if (this.bands.contains(PositionParameters.BAND_L5)) {
            bandsJson.put(BAND_L5, true)
        } else {
            bandsJson.put(BAND_L5, false)
        }

        return bandsJson
    }

    private fun getCorrectionsJson(): JSONObject {
        val correctionsJson = JSONObject()

        if (this.corrections.contains(PositionParameters.CORR_IONOSPHERE)) {
            correctionsJson.put(CORR_IONOSPHERE, true)
        } else {
            correctionsJson.put(CORR_IONOSPHERE, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_TROPOSPHERE)) {
            correctionsJson.put(CORR_TROPOSPHERE, true)
        } else {
            correctionsJson.put(CORR_TROPOSPHERE, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_MULTIPATH)) {
            correctionsJson.put(CORR_MULTIPATH, true)
        } else {
            correctionsJson.put(CORR_MULTIPATH, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_PPP)) {
            correctionsJson.put(CORR_PPP, true)
        } else {
            correctionsJson.put(CORR_PPP, false)
        }
        if (this.corrections.contains(PositionParameters.CORR_CAMERA)) {
            correctionsJson.put(CORR_CAMERA, true)
        } else {
            correctionsJson.put(CORR_CAMERA, false)
        }

        return correctionsJson
    }

    private fun getAlgorithmJson(): JSONObject {
        val algorithmJson = JSONObject()
        algorithmJson.put(ALG_LS, false)
        algorithmJson.put(ALG_WLS, false)
        algorithmJson.put(ALG_KALMAN, false)

        when (this.algorithm) {
            ALG_LS -> algorithmJson.put(ALG_LS, true)
            ALG_WLS -> algorithmJson.put(ALG_WLS, true)
            ALG_KALMAN -> algorithmJson.put(ALG_KALMAN, true)
            else -> algorithmJson.put(ALG_LS, true) // default value = LS
        }

        return algorithmJson
    }
}