package com.inari.team.computation

import com.google.android.gms.maps.model.LatLng
import com.inari.team.computation.data.ResponsePvtMultiConst
import com.inari.team.computation.infoextractors.getAcqInfo
import com.inari.team.computation.utils.Constants.CN0_MASK
import com.inari.team.computation.utils.Constants.ELEVATION_MASK
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.ResponsePvtMode

fun computePvt(gnssData: GnssData): List<ResponsePvtMode> {

    val responses = arrayListOf<ResponsePvtMode>()

    //Obtain Information
    var acqInformation = getAcqInfo(gnssData)

    //CN0 mask
    if (acqInformation.cn0mask != 0) {
        acqInformation = applyMask(acqInformation, CN0_MASK)
    }
    //Elevation mask
    if (acqInformation.elevationMask != 0) {
        acqInformation = applyMask(acqInformation, ELEVATION_MASK)
    }

    acqInformation.modes.forEach {

        val pvtMultiConst = try {
            pvtMultiConst(acqInformation, it)
        } catch (e: Exception) {
            ResponsePvtMultiConst()
        }

        if (pvtMultiConst.pvt.lat in -180.0..180.0 && pvtMultiConst.pvt.lng in -180.0..180.0) {

            val pvtResponse = ResponsePvtMode(
                refPosition = LatLng(
                    acqInformation.refLocation.refLocationLla.latitude,
                    acqInformation.refLocation.refLocationLla.longitude
                ),
                refAltitude = acqInformation.refLocation.refLocationLla.altitude.toFloat(),
                pvtLatLng = pvtMultiConst.pvt,
                modeColor = it.color,
                modeName = it.name,
                constellations = it.constellations,
                nSatellites = pvtMultiConst.nSats,
                galElevIono = pvtMultiConst.galElevIono,
                gpsElevIono = pvtMultiConst.gpsElevIono,
                gpsTime = pvtMultiConst.gpsTime
            )

            responses.add(pvtResponse)
        }

    }

    return responses
}