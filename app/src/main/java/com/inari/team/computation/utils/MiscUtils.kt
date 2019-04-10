package com.inari.team.computation.utils

import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.converters.lla2ecef
import com.inari.team.computation.data.EcefLocation
import com.inari.team.computation.data.LlaLocation
import com.inari.team.computation.data.PvtEcef
import com.inari.team.computation.data.PvtLatLng
import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM
import org.ejml.simple.SimpleMatrix
import java.lang.Math.*
import kotlin.math.pow

/**
 * Repairs over- and underflow of GPS clockBias
 */
fun checkTime(time: Double): Double {
    val halfWeek = 302400.0
    var newTime = time

    if (time > halfWeek) newTime = time - 2 * halfWeek
    if (time < -halfWeek) newTime = time + 2 * halfWeek

    return newTime
}

/**
 * Returns rotated satellite ECEF coordinates due to Earth rotation during signal travel clockBias
 */
fun earthRotCorr(travelTime: Double, xSat: DoubleArray): DoubleArray {
    val omegaeDot = 7.2921159e-5
    val omegaTau = omegaeDot * travelTime

    val matrixArray = doubleArrayOf(
        cos(omegaTau), sin(omegaTau), 0.0,    // 1st row
        -sin(omegaTau), cos(omegaTau), 0.0,   // 2nd row
        0.0, 0.0, 1.0                         // 3rd row
    )

    val rMat = DMatrixRMaj.wrap(3, 3, matrixArray)
    val xSatVec = DMatrixRMaj.wrap(1, 3, xSat)
    val xSatRotVec = DMatrixRMaj.wrap(1, 3, doubleArrayOf(0.0, 0.0, 0.0))

    // xSatRotVec = rMat * xSatVec
    MatrixVectorMult_DDRM.mult(rMat, xSatVec, xSatRotVec)

    return xSatRotVec.getData() // Transfor vector to DoubleArray
}

/**
 * Transforms clockBias in nanos to GPST
 */
fun nsgpst2gpst(timeNanos: Long): LongArray {
    val weekSeconds = 7L * 24L * 60L * 60L

    val timeSec = timeNanos / 1E9

    val now = floor(timeSec / weekSeconds).toLong()
    val tow = round(timeSec.rem(weekSeconds))

    return longArrayOf(tow, now)
}

/**
 * Compute Weight Matrix
 * C/N0 weighting method - Sigma e [Wieser, Andreas, et al. "An extended weight model for GPS phase observations"]
 */
fun computeCNoWeightMatrix(cnos: List<Double>, isWeight: Boolean): SimpleMatrix {
    var wMat = SimpleMatrix.identity(cnos.size)
    if (isWeight) {
        val diagonal = arrayListOf<Double>()

        cnos.forEach { cno ->
            val w = 0.244 * 10.0.pow(-0.1 * cno)
            diagonal.add(1/w)
        }

        wMat = SimpleMatrix.diag(*diagonal.toDoubleArray())
    }
    return wMat
}

fun pvtEcef2PvtLla(pvtEcef: PvtEcef): PvtLatLng{
    val posEcef = EcefLocation(pvtEcef.x, pvtEcef.y, pvtEcef.z)
    val posLla = ecef2lla(posEcef)
    return PvtLatLng(posLla.latitude, posLla.longitude, posLla.altitude, pvtEcef.clockBias)
}

fun pvtLla2PvtEcef(pvtLatLng: PvtLatLng): PvtEcef{
    val posLatLng = LlaLocation(pvtLatLng.lat, pvtLatLng.lng, pvtLatLng.altitude)
    val posEcef = lla2ecef(posLatLng)
    return PvtEcef(posEcef.x, posEcef.y, posEcef.z, pvtLatLng.clockBias)
}