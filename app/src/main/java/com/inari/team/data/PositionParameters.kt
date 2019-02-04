package com.inari.team.data

data class PositionParameters(
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
}