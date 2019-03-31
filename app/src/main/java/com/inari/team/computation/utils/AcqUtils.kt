package com.inari.team.computation.utils

import android.location.GnssMeasurement
import com.inari.team.computation.Constants.C
import com.inari.team.computation.converters.applyMod
import com.inari.team.computation.data.AcqInformationMeasurements
import com.inari.team.computation.data.GAL_E1C
import com.inari.team.computation.data.Satellite
import com.inari.team.computation.data.WEEK_NANOS

fun getTowDecodeSatellite(meas: GnssMeasurement, acqInformationMeasurements: AcqInformationMeasurements): Satellite {
    return with(meas) {
        val tTx = getTtx(timeOffsetNanos, receivedSvTimeNanos)
        val tRx = applyMod(acqInformationMeasurements.timeNanosGnss, WEEK_NANOS)
        Satellite(
            svid = svid,
            state = state,
            multiPath = multipathIndicator,
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
            multiPath = multipathIndicator,
            carrierFreq = carrierFrequencyHz.toDouble(),
            tTx = tTx,
            tRx = tRx,
            cn0 = cn0DbHz,
            pR = getPseudoRange(applyMod(tTx, GAL_E1C), applyMod(tRx, GAL_E1C))
        )
    }
}

fun checkTowDecode(tow: Int): Boolean {


    return true
}

fun checkTowKnown(tow: Int): Boolean {

    return true
}

fun checkGalState(tow: Int): Boolean {

    return true
}

fun getTtx(timeOffsetNanos: Double, receivedSvTimeNanos: Long): Double {
    return receivedSvTimeNanos + timeOffsetNanos
}

fun getPseudoRange(tTx: Double, tRx: Double): Double {
    return ((tRx - tTx) / 1000000000L) * C
}
