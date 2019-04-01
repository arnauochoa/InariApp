package com.inari.team.computation.corrections

import com.google.location.suplclient.asn1.supl2.rrlp_components.IonosphericModel
import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.converters.toTopocent
import com.inari.team.computation.data.EcefLocation
import com.inari.team.computation.data.PvtEcef
import com.inari.team.computation.data.Satellite


data class PropCorr(
    var tropoCorr: Double = 0.0,
    var ionoCorr: Double = 0.0
)

/**
 * Gets the propagation effects corrections
 */
fun getPropCorr(satPos: EcefLocation, refPos: PvtEcef, iono: ArrayList<Double>, tow: Double): PropCorr {

    val refPosArray = doubleArrayOf(refPos.x, refPos.y, refPos.z)
    val xPosArray = doubleArrayOf(satPos.x, satPos.y, satPos.z)

    // Coordinates transformation
    val topocentric = toTopocent(refPosArray, xPosArray)
    val llaRefPos = ecef2lla(EcefLocation(refPos.x, refPos.y, refPos.z))

    // Get tropospheric correction (Saastamoinen model)
    val tropoCorr = tropoErrorCorrection(topocentric.elevation, llaRefPos.altitude)

    return PropCorr()
}