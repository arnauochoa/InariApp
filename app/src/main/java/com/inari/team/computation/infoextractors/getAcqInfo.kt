package com.inari.team.computation.infoextractors

import android.location.GnssMeasurement
import com.inari.team.computation.converters.applyMod
import com.inari.team.computation.converters.lla2ecef
import com.inari.team.computation.data.*
import com.inari.team.computation.utils.*
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.GnssData
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

            meas.forEach { gnssMeas ->
                with(gnssMeas) {
                    when (constellationType) {
                        GnssStatus.CONSTELLATION_GPS -> {
                            if (multipathIndicator != 1) {
                                if (receivedSvTimeUncertaintyNanos != UNCERTAINTY_THR) {
                                    if (checkTowDecode(state)) {
                                        val sat = getTowDecodeSatellite(gnssMeas, acqInformationMeasurements)
                                        if (carrierFrequencyHz > FREQ_THR) {
                                            //L1
                                            acqInformationMeasurements.satellites.gpsSatellite.gpsL1.add(sat)
                                        } else {
                                            //L5
                                            acqInformationMeasurements.satellites.gpsSatellite.gpsL5.add(sat)
                                        }
                                    }
                                }
                            }
                        }
                        GnssStatus.CONSTELLATION_GALILEO -> {
                            if (multipathIndicator != 1) {
                                if (receivedSvTimeUncertaintyNanos != UNCERTAINTY_THR) {
                                    if (checkTowDecode(state)) {
                                        val sat = getTowDecodeSatellite(gnssMeas, acqInformationMeasurements)
                                        if (carrierFrequencyHz > FREQ_THR) {
                                            //GAL E1
                                            acqInformationMeasurements.satellites.galSatellites.galE1.add(sat)
                                        } else {
                                            //GAL E5a
                                            acqInformationMeasurements.satellites.galSatellites.galE5a.add(sat)
                                        }
                                    } else {
                                        if (checkTowKnown(gnssMeas.state)) {
                                            val sat = getTowDecodeSatellite(gnssMeas, acqInformationMeasurements)
                                            if (carrierFrequencyHz > FREQ_THR) {
                                                //GAL E1
                                                acqInformationMeasurements.satellites.galSatellites.galE1.add(sat)
                                            } else {
                                                //GAL E5a
                                                acqInformationMeasurements.satellites.galSatellites.galE5a.add(sat)
                                            }
                                        } else {
                                            if (checkGalState(gnssMeas.state)) {
                                                val sat = getE1CSatellite(gnssMeas, acqInformationMeasurements)
                                                if (carrierFrequencyHz > FREQ_THR) {
                                                    //GAL E1
                                                    acqInformationMeasurements.satellites.galSatellites.galE1.add(sat)
                                                } else {
                                                    //GAL E5a
                                                    acqInformationMeasurements.satellites.galSatellites.galE5a.add(sat)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }

    }

    return acqInformation

}

fun getTowDecodeSatellite(meas: GnssMeasurement, acqInformationMeasurements: AcqInformationMeasurements): Satellite {
    return with(meas) {
        val tTx = getTtx(timeOffsetNanos, receivedSvTimeNanos)
        val tRx = applyMod(acqInformationMeasurements.timeNanosGnss, WEEK_NANOS)
        Satellite(
            svid = svid,
            state = state,
            multipath = multipathIndicator,
            carrierFreq = carrierFrequencyHz.toDouble(),
            tTx = tTx,
            tRx = tRx,
            cn0 = cn0DbHz,
            pR = getPseudoRange(tTx, tRx)
        )
    }
}

fun getE1CSatellite(meas: GnssMeasurement, acqInformationMeasurements: AcqInformationMeasurements): Satellite {
    return with(meas) {
        val tTx = getTtx(timeOffsetNanos, receivedSvTimeNanos)
        val tRx = acqInformationMeasurements.timeNanosGnss
        Satellite(
            svid = svid,
            state = state,
            multipath = multipathIndicator,
            carrierFreq = carrierFrequencyHz.toDouble(),
            tTx = tTx,
            tRx = tRx,
            cn0 = cn0DbHz,
            pR = getPseudoRange(applyMod(tTx, GAL_E1C), applyMod(tRx, GAL_E1C))
        )
    }
}
