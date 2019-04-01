package com.inari.team.computation

import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.data.*
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.Constants.C
import com.inari.team.computation.utils.Constants.PVT_ITER
import com.inari.team.computation.utils.outliers
import com.inari.team.presentation.model.Mode
import org.ejml.data.DMatrixRMaj
import org.ejml.simple.SimpleMatrix
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

fun pvtMultiConst(acqInformation: AcqInformation, mode: Mode): ResponsePvtMultiConst {

    var pvtResponsePvtMultiConst = ResponsePvtMultiConst()

    val responseList = arrayListOf<ResponsePvtMultiConst>()

    acqInformation.acqInformationMeasurements.forEach { epoch ->

        var position = PvtEcef()
        var iono = arrayListOf<Double>()
        var responsePvtMultiConst = ResponsePvtMultiConst()

        var nGps = 0
        val gpsA = arrayListOf<DoubleArray>()
        val gpsP = arrayListOf<Double>()
        val gpsTcorr = arrayListOf<Double>()
        var gpsPcorr = arrayListOf<Double>()
        val gpsX = arrayListOf<EcefLocation>()
        val gpsPr = arrayListOf<Double>()
        val gpsSvn = arrayListOf<Int>()
        val gpsCn0 = arrayListOf<Double>()
        val gpsSatellites = arrayListOf<Satellite>()
        var gpsCorr: Double = 0.0
        var gpsPrC: Double = 0.0
        var gpsD0: Double = 0.0
        var gpsAx: Double = 0.0
        var gpsAy: Double = 0.0
        var gpsAz: Double = 0.0


        var nGal = 0
        var galA = arrayListOf<XYZ>()
        var galP = arrayListOf<Double>()
        var galTcorr = arrayListOf<Double>()
        var galPcorr = arrayListOf<Double>()
        var galX = ArrayList<EcefLocation>()
        var galPr = arrayListOf<Double>()
        var galSvn = arrayListOf<Int>()
        var galCn0 = arrayListOf<Double>()
        val galSatellites = arrayListOf<Satellite>()
        var galCorr: Double = 0.0
        var galPrC: Double = 0.0
        var galD0: Double = 0.0
        var galAx: Double = 0.0
        var galAy: Double = 0.0
        var galAz: Double = 0.0

        repeat(PVT_ITER) { i ->
            //LS

            if (i == 0) {
                //Initialize to the ref position
                position = PvtEcef(
                    acqInformation.refLocation.refLocationEcef.x,
                    acqInformation.refLocation.refLocationEcef.y,
                    acqInformation.refLocation.refLocationEcef.z
                )

                //initialize iono
                iono = epoch.ionoProto
            }

            if (mode.constellations.contains(Constants.GPS)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.L1)) {
                        epoch.satellites.gpsSatellites.gpsL1.forEach {
                            gpsPr.add(it.pR)
                            gpsSvn.add(it.svid)
                            gpsCn0.add(it.cn0)
                        }
                        nGps = epoch.satellites.gpsSatellites.gpsL1.size
                        gpsSatellites.addAll(epoch.satellites.gpsSatellites.gpsL1)
                    } else {
                        //L5
                        epoch.satellites.gpsSatellites.gpsL5.forEach {
                            gpsPr.add(it.pR)
                            gpsSvn.add(it.svid)
                            gpsCn0.add(it.cn0)
                        }
                        nGps = epoch.satellites.gpsSatellites.gpsL5.size
                        gpsSatellites.addAll(epoch.satellites.gpsSatellites.gpsL5)
                    }
                }

                for (j in 0 until nGps) {
                    if (i == 0) {
                        val ctrlCorr = getCtrlCorr(gpsSatellites[j], epoch.tow, gpsPr[j])
                        gpsX.add(ctrlCorr.ecefLocation)
                        gpsTcorr.add(ctrlCorr.tCorr)
                    }

                    gpsCorr = C * gpsTcorr[j]

                    //iono corrections

                    //tropo corrections

                    //2freq corrections

                    gpsPrC = gpsPr[j] + gpsCorr

                    //gps GeometricMatrix
                    if (gpsPrC != 0.0) {
                        gpsD0 = sqrt(
                            (gpsX[j].x - position.x).pow(2) +
                                    (gpsX[j].y - position.y).pow(2) +
                                    (gpsX[j].z - position.z).pow(2)
                        )

                        gpsP[j] = gpsPrC - gpsD0

                        gpsAx = -(gpsX[j].x - position.x) / gpsD0
                        gpsAy = -(gpsX[j].y - position.y) / gpsD0
                        gpsAz = -(gpsX[j].z - position.z) / gpsD0

                        gpsA.add(doubleArrayOf(gpsAx, gpsAy, gpsAz, 1.0))

                    }
                }
                val cleanSatsInd = outliers(gpsP)
                cleanSatsInd.forEach {
                    gpsP.removeAt(it)
                    gpsA.removeAt(it)
                    gpsCn0.removeAt(it)
                }

            }

            responsePvtMultiConst = leastSquares(position, gpsP, gpsA, false)

        }

        responseList.add(responsePvtMultiConst)
    }

    // Compute mean
    val nEpoch = responseList.size
    val pvtLatLng = PvtLatLng()
    var dop = Dop()
    var residue = 0.0
    var nSats = 0f
    var corrections = Corrections()
    responseList.forEach {
        pvtLatLng.lat += it.pvt.lat
        pvtLatLng.lng += it.pvt.lng
        pvtLatLng.altitude += it.pvt.altitude
        pvtLatLng.time += it.pvt.time

        dop.gDop += it.dop.gDop
        dop.pDop += it.dop.pDop
        dop.tDop += it.dop.tDop

        residue += it.residue

        nSats += it.nSats
    }

    pvtLatLng.lat = pvtLatLng.lat/nEpoch
    pvtLatLng.lng = pvtLatLng.lng/nEpoch
    pvtLatLng.altitude = pvtLatLng.altitude/nEpoch
    pvtLatLng.time = pvtLatLng.time/nEpoch

    dop.gDop = dop.gDop/nEpoch
    dop.pDop = dop.pDop/nEpoch
    dop.tDop = dop.tDop/nEpoch

    residue /= nEpoch

    nSats /= nEpoch

    pvtResponsePvtMultiConst = ResponsePvtMultiConst(pvtLatLng, dop, residue, corrections, nSats)

    return pvtResponsePvtMultiConst

}


fun leastSquares(
    position: PvtEcef,
    arrayPr: ArrayList<Double>,
    arrayA: ArrayList<DoubleArray>,
    multiC: Boolean
): ResponsePvtMultiConst {
    val nSats = arrayPr.size
    val nCols = if (multiC) 5 else 4
    if (arrayA.size != nSats) {
        Timber.d("A and p are not the same length")
    }

    // PVT computation
    val daPr = arrayPr.toDoubleArray()
    val daA = doubleArrayOf()
    repeat(nSats) { ind ->
        daA + arrayA[ind]
    }

    val prMat = DMatrixRMaj.wrap(nSats, 1, daPr)
    val aMat = DMatrixRMaj.wrap(nSats, nCols, daA)

    val prSMat = SimpleMatrix.wrap(prMat)
    val aSMat = SimpleMatrix.wrap(aMat)

    val invMat = aSMat.pseudoInverse()
    val dMat = invMat.mult(prSMat)

    val dArray = arrayListOf<Double>()
    repeat(nCols) { i ->
        dArray.add(dMat[i])
    }

    position.x += dArray[0]
    position.y += dArray[1]
    position.z += dArray[2]
    position.time += dArray[3]

    val llaLocation = ecef2lla(EcefLocation(position.x, position.y, position.z))
    val pvtLatLng = PvtLatLng(llaLocation.latitude, llaLocation.longitude, llaLocation.altitude, position.time)

    // DOP computation
    val gDop = sqrt(invMat[1, 1] + invMat[2, 2] + invMat[3, 3] + invMat[4, 4])
    val pDop = sqrt(invMat[1, 1] + invMat[2, 2] + invMat[3, 3])
    val tDop = sqrt(invMat[4, 4])
    val dop = Dop(gDop, pDop, tDop)

    // Resdiue computation
    val pEstMat = aSMat.mult(dMat)
    val resMat = prSMat.minus(pEstMat)

    val residue = sqrt(resMat[0].pow(2) + resMat[1].pow(2) + resMat[2].pow(2))


    val response = ResponsePvtMultiConst(pvtLatLng, dop, residue, Corrections(), nSats.toFloat())

    return response
}