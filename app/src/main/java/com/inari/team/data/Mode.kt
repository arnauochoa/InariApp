package com.inari.team.data

data class Mode(
    val id: Int,
    val name: String = "",
    val constellations: ArrayList<Int>,
    val bands: ArrayList<Int>,
    val corrections: ArrayList<Int>,
    val algorithm: Int
) {

    companion object {

        const val CONST_GPS: Int = 1
        const val CONST_GAL: Int = 2

        const val BAND_L1: Int = 1
        const val BAND_L5: Int = 2

        const val CORR_IONOSPHERE: Int = 1
        const val CORR_TROPOSPHERE: Int = 2
        const val CORR_MULTIPATH: Int = 3
        const val CORR_CAMERA: Int = 4

        const val ALG_LS: Int = 1
        const val ALG_WLS: Int = 2
        const val ALG_KALMAN: Int = 3

    }

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
        if (corrections.contains(CORR_MULTIPATH)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Multipath"
        }
        if (corrections.contains(CORR_CAMERA)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Camera"
        }
        if (correctionsString.isBlank()) correctionsString = "None"
        return correctionsString
    }

    fun algorithmAsString(): CharSequence? {
        var algorithmString = ""

        when (algorithm) {
            ALG_LS -> algorithmString = "Least Squares"
            ALG_WLS -> algorithmString = "Weighted Least Squares"
            ALG_KALMAN -> algorithmString = "Kalman Filter"
        }
        return algorithmString
    }

}
