package com.inari.team.computation

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.inari.team.computation.data.ResponsePvtMultiConst
import com.inari.team.computation.infoextractors.getAcqInfo
import com.inari.team.computation.utils.Constants.CN0_MASK
import com.inari.team.computation.utils.Constants.ELEVATION_MASK
import com.inari.team.core.utils.saveGnssFile
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.ResponsePvtMode
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.util.*

fun computePvt(gnssData: GnssData): List<ResponsePvtMode> {

    val responses = arrayListOf<ResponsePvtMode>()

    //Obtain Information
    var acqInformation = getAcqInfo(gnssData)

    //todo remove after testing
    val savedAcqInformationLog = acqInformation

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

            //todo remove after testing
            try {

                val acq = Gson().toJson(savedAcqInformationLog)
                saveGnssFile("${Date()}.txt", ResponseBody.create(MediaType.parse("text/plain"), acq))
            } catch (e: Exception){

            }

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

    }

    return responses
}