package com.inari.team.presentation.model

object PositionParameters {
    const val CONSTELLATIONS_KEY = "constellations"
    const val CONST_GPS_STR: String = "GPS"
    const val CONST_GAL_STR: String = "Galileo"
    const val CONST_GLO_STR: String = "GLONASS"

    const val BANDS_KEY = "bands"
    const val BAND_L1_STR = "L1"
    const val BAND_L5_STR = "L5"

    const val CORRECTIONS_KEY = "corrections"
    const val CORR_IONOSPHERE_STR = "ionosphere"
    const val CORR_TROPOSPHERE_STR = "troposphere"
    const val CORR_IONOFREE_STR = "ionofree"

    const val ALGORITHM_KEY = "algorithm"
    const val ALG_LS_STR = "LS"
    const val ALG_WLS_STR = "WLS"
    const val ALG_KALMAN_STR = "KAL"

    const val AVERAGING_TIME_SEC_1 = 5L

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