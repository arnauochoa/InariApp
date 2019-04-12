package com.inari.team.computation.utils

import android.location.GnssMeasurement
import com.inari.team.computation.data.AcqInformationMeasurements
import com.inari.team.computation.data.GAL_E1C
import com.inari.team.computation.data.Satellite
import com.inari.team.computation.data.WEEK_NANOS
import com.inari.team.computation.utils.Constants.C
import com.inari.team.computation.utils.Constants.L1_FREQ
import java.math.BigInteger

fun getTowDecodeSatellite(meas: GnssMeasurement, acqInformationMeasurements: AcqInformationMeasurements): Satellite {
    return with(meas) {
        val tTx = getTtx(timeOffsetNanos, receivedSvTimeNanos)
        val tRx = applyMod(acqInformationMeasurements.timeNanosGnss, WEEK_NANOS)
        Satellite(
            svid = svid,
            state = state,
            multiPath = multipathIndicator,
            carrierFreq = if (meas.hasCarrierFrequencyHz()) carrierFrequencyHz.toDouble() else L1_FREQ,
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
            pR = getPseudoRange(tTx, applyMod(tRx, GAL_E1C))
        )
    }
}


fun checkTowDecode(state: Int): Boolean {
    // Check if binary state has 3rd bit 1
    return (state and GnssMeasurement.STATE_TOW_DECODED) == GnssMeasurement.STATE_TOW_DECODED
}

fun checkTowKnown(state: Int): Boolean {
    // Check if binary state has 14th bit 1
    return (state and GnssMeasurement.STATE_TOW_KNOWN) == GnssMeasurement.STATE_TOW_KNOWN
}

fun checkGalState(state: Int): Boolean {
    // Check if binary state has 14th bit 1
    return (state and GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) == GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK
}

fun getTtx(timeOffsetNanos: Double, receivedSvTimeNanos: Long): Double {
    return receivedSvTimeNanos + timeOffsetNanos
}

fun getPseudoRange(tTx: Double, tRx: Double): Double {
    return ((tRx - tTx) / 1000000000L) * C
}
