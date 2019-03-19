package com.inari.team.presentation.model

data class GnssSettings(
    var constellation: ArrayList<Int> = DEFAULT_CONSTELLATION,
    var band: ArrayList<Int> = DEFAULT_BAND,
    var corrections: ArrayList<Int> = DEFAULT_CORRECTIONS,
    var algorithm: Int = DEFAULT_ALGORITHM,
    var enableAveraging: Boolean = DEFAULT_ENABLE_AVERAGING,
    var averagingTime: Long? = null
){
    companion object {
        const val CONST_GPS = 1
        const val CONST_GAL = 2

        const val BAND_L1 = 1
        const val BAND_L5 = 2

        const val CORR_IONOSPHERE = 1
        const val CORR_TROPOSPHERE = 2
        const val CORR_MASKING = 3

        const val ALG_LS = 1
        const val ALG_WLS = 2
        const val ALG_KALMAN = 3

        val DEFAULT_CONSTELLATION = arrayListOf(CONST_GPS)
        val DEFAULT_BAND = arrayListOf(BAND_L1)
        val DEFAULT_CORRECTIONS = arrayListOf(CORR_IONOSPHERE, CORR_TROPOSPHERE)
        const val DEFAULT_ALGORITHM = ALG_LS
        const val DEFAULT_ENABLE_AVERAGING = false
    }
}