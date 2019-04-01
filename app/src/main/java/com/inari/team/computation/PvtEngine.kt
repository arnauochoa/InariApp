package com.inari.team.computation

import com.google.android.gms.maps.model.LatLng
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

        val pvtMultiConst = pvtMultiConst(acqInformation, it)

        val pvtResponse = ResponsePvtMode(
            LatLng(
                acqInformation.refLocation.refLocationLla.latitude,
                acqInformation.refLocation.refLocationLla.longitude
            ),
            acqInformation.refLocation.refLocationLla.altitude.toFloat(),
            LatLng(pvtMultiConst.pvt.lat, pvtMultiConst.pvt.lng),
            it.color,
            it.name
        )

        responses.add(pvtResponse)

    }

    return responses
}