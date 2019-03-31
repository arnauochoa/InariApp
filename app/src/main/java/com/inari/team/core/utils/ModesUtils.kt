package com.inari.team.core.utils

import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.PositionParameters

fun addDefaultModes(): List<Mode> {

    val mode = Mode(
        0,
        "GPS LS",
        arrayListOf(PositionParameters.CONST_GPS),
        arrayListOf(PositionParameters.BAND_L1),
        arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
        PositionParameters.ALG_LS,
        isSelected = false
    )
    val mode2 = Mode(
        1,
        "Galileo LS",
        arrayListOf(PositionParameters.CONST_GAL),
        arrayListOf(PositionParameters.BAND_L1),
        arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
        PositionParameters.ALG_LS,
        isSelected = false
    )
    val mode3 = Mode(
        2,
        "GPS WLS",
        arrayListOf(PositionParameters.CONST_GPS),
        arrayListOf(PositionParameters.BAND_L1),
        arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
        PositionParameters.ALG_WLS,
        isSelected = false
    )

    val mode4 = Mode(
        3,
        "Galileo WLS",
        arrayListOf(PositionParameters.CONST_GAL),
        arrayListOf(PositionParameters.BAND_L1),
        arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
        PositionParameters.ALG_WLS,
        isSelected = false
    )

    val mode5 = Mode(
        4,
        "Multiconst Iono-Free",
        arrayListOf(PositionParameters.CONST_GPS, PositionParameters.CONST_GAL),
        arrayListOf(PositionParameters.BAND_L1, PositionParameters.BAND_L5),
        arrayListOf(PositionParameters.CORR_TROPOSPHERE, PositionParameters.CORR_IONOFREE),
        PositionParameters.ALG_WLS,
        isSelected = false
    )

    return arrayListOf(mode, mode2, mode3, mode4, mode5)

}