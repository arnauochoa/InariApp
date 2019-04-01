package com.inari.team.computation

import com.inari.team.computation.infoextractors.getAcqInfo
import com.inari.team.computation.utils.Constants.CN0_MASK
import com.inari.team.computation.utils.Constants.ELEVATION_MASK
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.ResponsePvtMode

fun computePvt(gnssData: GnssData): List<ResponsePvtMode> {

    val responses = arrayListOf<ResponsePvtMode>()

    //Obtain Information
    val acqInformation = getAcqInfo(gnssData)

    //CN0 mask
    if (acqInformation.cn0mask != 0) {
        applyMask(acqInformation, CN0_MASK)
    }
    //Elevation mask
    if (acqInformation.elevationMask != 0) {
        applyMask(acqInformation, ELEVATION_MASK)
    }

    acqInformation.modes.forEach {

        pvtMultiConst(acqInformation, it)

    }

    return responses
}