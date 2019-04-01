package com.inari.team.computation

import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.corrections.getCtrlCorr
import com.inari.team.computation.corrections.getPropCorr
import com.inari.team.computation.data.*
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.Constants.C
import com.inari.team.computation.utils.Constants.PVT_ITER
import com.inari.team.computation.utils.outliers
import com.inari.team.presentation.model.Mode
import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

@Throws(Exception::class)
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
        var gpsCorr: Double
        var gpsPrC: Double
        var gpsD0: Double
        var gpsAx: Double
        var gpsAy: Double
        var gpsAz: Double


        val nGal = 0
        val galA = arrayListOf<DoubleArray>()
        val galP = arrayListOf<Double>()
        val galTcorr = arrayListOf<Double>()
        var galPcorr = arrayListOf<Double>()
        val galX = ArrayList<EcefLocation>()
        val galPr = arrayListOf<Double>()
        val galSvn = arrayListOf<Int>()
        val galCn0 = arrayListOf<Double>()
        val galSatellites = arrayListOf<Satellite>()
        var galCorr: Double
        var galPrC: Double
        var galD0: Double
        var galAx: Double
        var galAy: Double
        var galAz: Double

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
                        val ctrlCorr =
                            getCtrlCorr(gpsSatellites[j], epoch.tow, gpsPr[j])
                        gpsX.add(ctrlCorr.ecefLocation)
                        gpsTcorr.add(ctrlCorr.tCorr)
                    }

                    gpsCorr = C * gpsTcorr[j]

                    //Propagation corrections
                    val propCorr = getPropCorr(gpsX[j], position, epoch.ionoProto, epoch.tow, mode.corrections)

                    //2freq corrections

                    gpsPrC = gpsPr[j] + gpsCorr

                    //gps GeometricMatrix
                    if (gpsPrC != 0.0) {
                        gpsD0 = sqrt(
                            (gpsX[j].x - position.x).pow(2) +
                                    (gpsX[j].y - position.y).pow(2) +
                                    (gpsX[j].z - position.z).pow(2)
                        )

                        gpsP.add(j, gpsPrC - gpsD0)

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

                try {
                    responsePvtMultiConst = leastSquares(position, gpsP, gpsA, false)
                } catch (e: Exception) {
                    Timber.d(e.localizedMessage)
                }

            }

            if (mode.constellations.contains(Constants.GALILEO)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.E1)) {
                        epoch.satellites.galSatellites.galE1.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCn0.add(it.cn0)
                        }
                        nGps = epoch.satellites.galSatellites.galE1.size
                        gpsSatellites.addAll(epoch.satellites.galSatellites.galE1)
                    } else {
                        //L5
                        epoch.satellites.galSatellites.galE5a.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCn0.add(it.cn0)
                        }
                        nGps = epoch.satellites.galSatellites.galE5a.size
                        gpsSatellites.addAll(epoch.satellites.galSatellites.galE5a)
                    }
                }

                for (j in 0 until nGal) {
                    if (i == 0) {
                        val ctrlCorr =
                            getCtrlCorr(galSatellites[j], epoch.tow, galPr[j])
                        galX.add(ctrlCorr.ecefLocation)
                        galTcorr.add(ctrlCorr.tCorr)
                    }

                    galCorr = C * galTcorr[j]

                    //iono corrections

                    //tropo corrections

                    //2freq corrections

                    galPrC = galPr[j] + galCorr

                    //gps GeometricMatrix
                    if (galPrC != 0.0) {
                        galD0 = sqrt(
                            (galX[j].x - position.x).pow(2) +
                                    (galX[j].y - position.y).pow(2) +
                                    (galX[j].z - position.z).pow(2)
                        )

                        galP.add(j, galPrC - galD0)

                        galAx = -(galX[j].x - position.x) / galD0
                        galAy = -(galX[j].y - position.y) / galD0
                        galAz = -(galX[j].z - position.z) / galD0

                        galA.add(doubleArrayOf(galAx, galAy, galAz, 1.0))

                    }
                }
                val cleanSatsInd = outliers(galP)
                cleanSatsInd.forEach {
                    galP.removeAt(it)
                    galA.removeAt(it)
                    galCn0.removeAt(it)
                }

                try {
                    responsePvtMultiConst = leastSquares(position, galP, galA, false)
                } catch (e: Exception) {
                    Timber.d(e.localizedMessage)
                }

            }

        }



        if (responsePvtMultiConst.pvt.lat != 360.0) {
            responseList.add(responsePvtMultiConst)
        }
    }

    // Compute mean
    val nEpoch = responseList.size
    if (nEpoch > 0) {
        val pvtLatLng = PvtLatLng(0.0, 0.0, 0.0, 0.0)
        var dop = Dop(0.0, 0.0, 0.0)
        var residue = 0.0
        var nSats = 0f
        var corrections = Corrections(0.0, 0.0, 0.0, 0.0, 0.0)
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
        pvtLatLng.lat = pvtLatLng.lat / nEpoch
        pvtLatLng.lng = pvtLatLng.lng / nEpoch
        pvtLatLng.altitude = pvtLatLng.altitude / nEpoch
        pvtLatLng.time = pvtLatLng.time / nEpoch

        dop.gDop = dop.gDop / nEpoch
        dop.pDop = dop.pDop / nEpoch
        dop.tDop = dop.tDop / nEpoch

        residue /= nEpoch

        nSats /= nEpoch

        pvtResponsePvtMultiConst = ResponsePvtMultiConst(pvtLatLng, dop, residue, corrections, nSats)
    }

    return pvtResponsePvtMultiConst
}


@Throws(Exception::class)
fun leastSquares(
    position: PvtEcef,
    arrayPr: ArrayList<Double>,
    arrayA: ArrayList<DoubleArray>,
    multiC: Boolean
): ResponsePvtMultiConst {
    val nSats = arrayPr.size
    val nCols = if (multiC) 5 else 4
    var response = ResponsePvtMultiConst()
    if (nSats >= nCols) {
        if (arrayA.size != nSats) {
            Timber.d("A and p are not the same length")
        }

        // PVT computation
        val daPr = arrayPr.toDoubleArray()
        var daA = doubleArrayOf()
        repeat(nSats) { ind ->
            daA += arrayA[ind]
        }

        val prMat = DMatrixRMaj.wrap(nSats, 1, daPr)
        val aMat = DMatrixRMaj.wrap(nSats, nCols, daA)

        var temp = doubleArrayOf()
        repeat(nCols * nSats) {
            temp += 0.0
        }
        val invMat = DMatrixRMaj.wrap(nCols, nSats, temp)

        CommonOps_DDRM.pinv(aMat, invMat)
        temp = doubleArrayOf()
        repeat(nCols) {
            temp += 0.0
        }
        val dMat = DMatrixRMaj.wrap(nCols, 1, temp)
        CommonOps_DDRM.mult(invMat, prMat, dMat)


        val dArray = arrayListOf<Double>()
        repeat(nCols) { i ->
            dArray.add(dMat[i])
        }

        position.x += dArray[0]
        position.y += dArray[1]
        position.z += dArray[2]
        position.time += dArray[3] / C

        val llaLocation = ecef2lla(EcefLocation(position.x, position.y, position.z))
        val pvtLatLng = PvtLatLng(llaLocation.latitude, llaLocation.longitude, llaLocation.altitude, position.time)

        // DOP computation
//        val gDop = sqrt(invMat[0, 0] + invMat[1, 1] + invMat[2, 2] + invMat[3, 3])
//        val pDop = sqrt(invMat[0, 0] + invMat[1, 1] + invMat[2, 2])
//        val tDop = sqrt(invMat[3, 3])
//        val dop = Dop(gDop, pDop, tDop)
        val dop = Dop()

        // Residue computation
        temp = doubleArrayOf()
        repeat(nSats) {
            temp += 0.0
        }
        val pEstMat = DMatrixRMaj.wrap(nSats, 1, temp)
        CommonOps_DDRM.mult(aMat, dMat, pEstMat)
        val resMat = DMatrixRMaj.wrap(nSats, 1, temp)
        CommonOps_DDRM.subtract(prMat, pEstMat, resMat)

        val residue = sqrt(resMat[0].pow(2) + resMat[1].pow(2) + resMat[2].pow(2))

        response = ResponsePvtMultiConst(pvtLatLng, dop, residue, Corrections(), nSats.toFloat())
    }

    return response
}