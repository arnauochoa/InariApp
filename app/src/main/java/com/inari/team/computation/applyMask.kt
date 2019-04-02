package com.inari.team.computation

import com.inari.team.computation.utils.Constants.CN0_MASK
import com.inari.team.computation.utils.Constants.ELEVATION_MASK
import com.inari.team.computation.data.AcqInformation

fun applyMask(acqInformation: AcqInformation, maskType: Int): AcqInformation {

    acqInformation.acqInformationMeasurements.forEach {

        val filteredL1 = it.satellites.gpsSatellites.gpsL1.filter { sat ->
            when (maskType) {
                CN0_MASK -> {
                    sat.cn0 >= acqInformation.cn0mask
                }
                ELEVATION_MASK -> {
                    sat.elevation >= acqInformation.elevationMask
                }
                else -> true
            }
        }
        it.satellites.gpsSatellites.gpsL1.clear()
        it.satellites.gpsSatellites.gpsL1.addAll(filteredL1)

        val filteredL5 = it.satellites.gpsSatellites.gpsL5.filter { sat ->
            when (maskType) {
                CN0_MASK -> {
                    sat.cn0 >= acqInformation.cn0mask
                }
                ELEVATION_MASK -> {
                    sat.elevation >= acqInformation.elevationMask
                }
                else -> true
            }
        }
        it.satellites.gpsSatellites.gpsL5.clear()
        it.satellites.gpsSatellites.gpsL5.addAll(filteredL5)

        val filteredE1 = it.satellites.galSatellites.galE1.filter { sat ->
            when (maskType) {
                CN0_MASK -> {
                    sat.cn0 >= acqInformation.cn0mask
                }
                ELEVATION_MASK -> {
                    sat.elevation >= acqInformation.elevationMask
                }
                else -> true
            }
        }
        it.satellites.galSatellites.galE1.clear()
        it.satellites.galSatellites.galE1.addAll(filteredE1)

        val filteredE5a = it.satellites.galSatellites.galE5a.filter { sat ->
            when (maskType) {
                CN0_MASK -> {
                    sat.cn0 >= acqInformation.cn0mask
                }
                ELEVATION_MASK -> {
                    sat.elevation >= acqInformation.elevationMask
                }
                else -> true
            }
        }
        it.satellites.galSatellites.galE5a.clear()
        it.satellites.galSatellites.galE5a.addAll(filteredE5a)

    }

    return acqInformation

}