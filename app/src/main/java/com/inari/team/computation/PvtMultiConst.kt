package com.inari.team.computation

import com.inari.team.computation.data.AcqInformation
import com.inari.team.computation.data.PvtEcef
import com.inari.team.computation.data.ResponsePvtMultiConst
import com.inari.team.computation.data.XYZBias
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.Constants.PVT_ITER
import com.inari.team.presentation.model.Mode

fun pvtMultiConst(acqInformation: AcqInformation, eph: HashMap<String, Any>, mode: Mode): ResponsePvtMultiConst {

    val pvtResponsePvtMultiConst = ResponsePvtMultiConst()

    acqInformation.acqInformationMeasurements.forEach { meas ->

        var position = PvtEcef()
        var iono = arrayListOf<Double>()

        var nGps = 0
        var gpsA = arrayListOf<XYZBias>()
        var gpsP = arrayListOf<Double>()
        var gpsTcorr = arrayListOf<Double>()
        var gpsPcorr = arrayListOf<Double>()
//        var gpsX = arrayListOf<>()

        repeat(PVT_ITER) { i ->

            if (i == 0) {
                //Initialize to the ref position
                position = PvtEcef(
                    acqInformation.refLocation.refLocationEcef.x,
                    acqInformation.refLocation.refLocationEcef.y,
                    acqInformation.refLocation.refLocationEcef.z
                )

                //initialize iono
                iono = meas.ionoProto
            }

            if (mode.constellations.contains(Constants.GPS)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.L1)) {

                    } else {

                    }
                }

            }

        }


    }

    return pvtResponsePvtMultiConst

}