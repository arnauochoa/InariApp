package com.inari.team.computation.infoextractors

import com.inari.team.computation.converters.applyMod
import com.inari.team.computation.converters.lla2ecef
import com.inari.team.computation.data.*
import com.inari.team.computation.utils.checkTowDecode
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.PositionParameters
import kotlin.math.floor

fun getAcqInfo(gnssData: GnssData): AcqInformation {

    val acqInformation = AcqInformation()

    //Location
    gnssData.location?.let {
        val refLocationLla = RefLocationLla(it.latitude, it.longitude, it.altitude)
        acqInformation.refLocation = RefLocationData(
            refLocationLla,
            lla2ecef(refLocationLla)
        )
    }

    //Gnss Raw Measurements
    gnssData.measurements.forEach {
        val acqInformationMeasurements = AcqInformationMeasurements()

        //Clock Info
        it.gnssClock?.let { gnssClock ->

            acqInformationMeasurements.timeNanosGnss = if (gnssClock.hasBiasNanos()) {
                gnssClock.timeNanos - (gnssClock.biasNanos + gnssClock.fullBiasNanos)
            } else {
                (gnssClock.timeNanos - gnssClock.fullBiasNanos).toDouble()
            }

            acqInformationMeasurements.tow =
                floor(applyMod(acqInformationMeasurements.timeNanosGnss, WEEK_NANOS))
            acqInformationMeasurements.now =
                floor(acqInformationMeasurements.timeNanosGnss / WEEK_NANOS)
        }

        //Measurements
        it.gnssMeasurements?.let { meas ->

            meas.forEach {gnssMeas -> {
                when(gnssMeas.constellationType){
                    GnssStatus.CONSTELLATION_GPS -> {
                        if (gnssMeas.multipathIndicator != 1){
                            if (gnssMeas.receivedSvTimeUncertaintyNanos != UNCERTAINTY_THR){
                                if (checkTowDecode(gnssMeas.state)){

                                }
                            }
                        }
                    }
                    GnssStatus.CONSTELLATION_GALILEO ->{

                    }
                }
            }

            }

            acqInformationMeasurements.satellites

        }

    }

    return acqInformation

}