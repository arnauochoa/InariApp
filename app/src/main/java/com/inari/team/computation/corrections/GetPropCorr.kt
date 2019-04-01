package com.inari.team.computation.corrections

import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.converters.toTopocent
import com.inari.team.computation.data.EcefLocation
import com.inari.team.computation.data.PvtEcef
import com.inari.team.computation.utils.Constants.KLOBUCHAR
import com.inari.team.presentation.model.PositionParameters


data class PropCorr(
    var tropoCorr: Double = 0.0,
    var ionoCorr: Double = 0.0
)

/**
 * Gets the propagation effects corrections
 */
fun getPropCorr(
    satPos: EcefLocation,
    refPos: PvtEcef,
    iono: ArrayList<Double>,
    tow: Double,
    corrections: ArrayList<Int>
): PropCorr {

    val refPosArray = doubleArrayOf(refPos.x, refPos.y, refPos.z)
    val xPosArray = doubleArrayOf(satPos.x, satPos.y, satPos.z)

    // Coordinates transformation
    val topoSatPos = toTopocent(refPosArray, xPosArray)
    val llaRefPos = ecef2lla(EcefLocation(refPos.x, refPos.y, refPos.z))

    val tropoCorr = if (corrections.contains(PositionParameters.CORR_TROPOSPHERE)) {
        // Get tropospheric correction (Saastamoinen model)
        tropoErrorCorrection(arrayListOf(topoSatPos.elevation), arrayListOf(llaRefPos.altitude))
    } else 0.0


    val ionoCorr = if (corrections.contains(PositionParameters.CORR_IONOSPHERE)) {
        // Get ionospheric correction
        if (iono.isNotEmpty()) {
            ionoErrorCorrections(llaRefPos, topoSatPos, tow, iono, KLOBUCHAR)
        } else {
            0.00001
        }
    } else 0.0

    return PropCorr(tropoCorr, ionoCorr)
}