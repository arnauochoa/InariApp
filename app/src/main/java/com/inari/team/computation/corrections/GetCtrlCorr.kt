package com.inari.team.computation.corrections

import com.inari.team.computation.data.EcefLocation
import com.inari.team.computation.data.Satellite
import com.inari.team.computation.satPos
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.Constants.C
import com.inari.team.computation.utils.checkTime
import com.inari.team.computation.utils.earthRotCorr
import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import kotlin.math.pow

fun getCtrlCorr(
    satellite: Satellite,
    tow: Double,
    pR: Double,
    constellation: Int,
    band: Int = -1 //just needed when it is Galileo E5a band
): CtrlCorr {

    // Compute transmission clockBias
    val txRaw = tow - pR / C

    // Get clock corrections
    var tCorr = satClockErrorCorrection(txRaw, satellite)

    tCorr -= if (band == Constants.E5A) satellite.tgdS * (Constants.L1_FREQ / Constants.L5_FREQ).pow(2) else satellite.tgdS

    var txGPS = txRaw - tCorr

    // Compute again the clock bias
    tCorr = satClockErrorCorrection(txGPS, satellite)
    tCorr -= satellite.tgdS

    // Get the satellite coordinates (corrected) and velocity
    val satPos = satPos(txGPS, satellite, constellation)
    var x = satPos.x
    val vel = satPos.vel

    val xVec = DMatrixRMaj.wrap(3, 1, x)
    val velVec = DMatrixRMaj.wrap(3, 1, vel)

    // Get the satellite relativistic clock correction
    val tRel: Double = -2 * (CommonOps_DDRM.dot(xVec, velVec) / (C * C))

    // Account for the relativistic effect on the satellite clock bias and the clockBias of transmission
    tCorr += tRel
    txGPS = txRaw - tCorr

    // Recompute the satellite coordinates with the corrected clockBias and some additional
    // correction (i.e. Sagnac effect / rotation correction)
    val travelTime = tow - txGPS
    x = earthRotCorr(travelTime, x)

    return CtrlCorr(EcefLocation(x[0], x[1], x[2]), tCorr)
}

fun satClockErrorCorrection(time: Double, satellite: Satellite): Double {
    var corr: Double
    with(satellite) {
        var dt = 0.0
        keplerModel?.let {
            dt = checkTime(time - it.toeS)
        }
        corr = (af2 * dt + af1) * dt + af0
    }
    return corr
}

data class CtrlCorr(
    var ecefLocation: EcefLocation,
    var tCorr: Double = 0.0
)
