package com.inari.team.computation

import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.corrections.getCtrlCorr
import com.inari.team.computation.corrections.getIonoCorrDualFreq
import com.inari.team.computation.corrections.getPropCorr
import com.inari.team.computation.data.*
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.Constants.C
import com.inari.team.computation.utils.Constants.GALILEO
import com.inari.team.computation.utils.Constants.GPS
import com.inari.team.computation.utils.Constants.PVT_ITER
import com.inari.team.computation.utils.computeCNoWeightMatrix
import com.inari.team.computation.utils.outliers
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.PositionParameters
import com.inari.team.presentation.model.PositionParameters.ALG_WLS
import kotlinx.coroutines.processNextEventInCurrentThread
import org.ejml.simple.SimpleMatrix
import timber.log.Timber
import kotlin.math.pow
import kotlin.math.sqrt

@Throws(Exception::class)
fun pvtMultiConst(acqInformation: AcqInformation, mode: Mode): ResponsePvtMultiConst {

    var pvtResponsePvtMultiConst = ResponsePvtMultiConst()

    val responseList = arrayListOf<ResponsePvtMultiConst>()

    val isMultiConst = mode.constellations.contains(Constants.GPS) && mode.constellations.contains(Constants.GALILEO)
    val isWeight = mode.algorithm == ALG_WLS

    var position = PvtEcef()
    acqInformation.acqInformationMeasurements.forEach { epoch ->

        var iono = arrayListOf<Double>()
        var responsePvtMultiConst = ResponsePvtMultiConst()

        var nGps = 0
        val gpsA = arrayListOf<ArrayList<Double>>()
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


        var nGal = 0
        val galA = arrayListOf<ArrayList<Double>>()
        val galP = arrayListOf<Double>()
        val galTcorr = arrayListOf<Double>()
        var galPcorr = arrayListOf<Double>()
        val galX = ArrayList<EcefLocation>()
        val galPr = arrayListOf<Double>()
        val galSvn = arrayListOf<Int>()
        val galCn0 = arrayListOf<Double>()
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

            //LOOP FOR GPS
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
                            getCtrlCorr(gpsSatellites[j], epoch.tow, gpsPr[j], Constants.GPS)
                        gpsX.add(ctrlCorr.ecefLocation)
                        gpsTcorr.add(ctrlCorr.tCorr)
                    }

                    gpsCorr = C * gpsTcorr[j]

                    //Propagation corrections
                    val propCorr = getPropCorr(gpsX[j], position, epoch.ionoProto, epoch.tow, mode.corrections)
                    gpsCorr = gpsCorr - propCorr.tropoCorr - propCorr.ionoCorr
                    gpsPrC = gpsPr[j]

                    //2freq corrections
                    if (mode.corrections.contains(PositionParameters.CORR_IONOFREE)) {
                        // todo if there are no measurements with two frequencies, use ephemeris iono corrections
                        var pr1 = 0.0
                        var freq1 = 0.0
                        var pr2 = 0.0
                        var freq2 = 0.0

                        epoch.satellites.gpsSatellites.gpsL1.forEach { s ->
                            if (s.svid == gpsSvn[j]) {
                                pr1 = s.pR
                                freq1 = s.carrierFreq
                            }
                        }
                        epoch.satellites.gpsSatellites.gpsL5.forEach { s ->
                            if (s.svid == gpsSvn[j]) {
                                pr2 = s.pR
                                freq2 = s.carrierFreq
                            }
                        }

                        if (pr1 != 0.0 && pr2 != 0.00 && freq1 != 0.0 && freq2 != 0.00) {
                            pr1 += gpsCorr
                            pr2 += gpsCorr
                            gpsPrC = getIonoCorrDualFreq(arrayListOf(freq1, freq2), arrayListOf(pr1, pr2))
                        }
                    } else {
                        gpsPrC = gpsPr[j] + gpsCorr
                    }

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

                        val row = arrayListOf(gpsAx, gpsAy, gpsAz, 1.0)
                        if (isMultiConst) row.add(0.0)
                        gpsA.add(row)

                    }
                }

                //pseudorange "RAIM"
                val cleanSatsInd = outliers(gpsP)
                cleanSatsInd.forEach {
                    gpsP.removeAt(it)
                    gpsA.removeAt(it)
                    gpsCn0.removeAt(it)
                }

            }

            //LOOP FOR GALILEO
            if (mode.constellations.contains(Constants.GALILEO)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.E1)) {
                        epoch.satellites.galSatellites.galE1.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCn0.add(it.cn0)
                        }
                        nGal = epoch.satellites.galSatellites.galE1.size
                        galSatellites.addAll(epoch.satellites.galSatellites.galE1)
                    } else {
                        //L5
                        epoch.satellites.galSatellites.galE5a.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCn0.add(it.cn0)
                        }
                        nGal = epoch.satellites.galSatellites.galE5a.size
                        galSatellites.addAll(epoch.satellites.galSatellites.galE5a)
                    }
                }

                for (j in 0 until nGal) {
                    if (i == 0) {
                        val ctrlCorr = if (mode.bands.contains(Constants.E5A)) {
                            getCtrlCorr(galSatellites[j], epoch.tow, galPr[j], Constants.GALILEO, Constants.E5A)
                        } else {
                            getCtrlCorr(galSatellites[j], epoch.tow, galPr[j], Constants.GALILEO)
                        }
                        galX.add(ctrlCorr.ecefLocation)
                        galTcorr.add(ctrlCorr.tCorr)
                    }

                    galCorr = C * galTcorr[j]

                    //Propagation corrections
                    val propCorr = getPropCorr(galX[j], position, epoch.ionoProto, epoch.tow, mode.corrections)
                    galCorr = galCorr - propCorr.tropoCorr - propCorr.ionoCorr


                    //2freq corrections
                    if (mode.corrections.contains(PositionParameters.CORR_IONOFREE)) {
                        var pr1 = 0.0
                        var freq1 = 0.0
                        var pr2 = 0.0
                        var freq2 = 0.0

                        epoch.satellites.galSatellites.galE1.forEach { s ->
                            if (s.svid == gpsSvn[j]) {
                                pr1 = s.pR
                                freq1 = s.carrierFreq
                            }
                        }
                        epoch.satellites.galSatellites.galE5a.forEach { s ->
                            if (s.svid == gpsSvn[j]) {
                                pr2 = s.pR
                                freq2 = s.carrierFreq
                            }
                        }

                        if (pr1 != 0.0 && pr2 != 0.00 && freq1 != 0.0 && freq2 != 0.00) {
                            galPrC = getIonoCorrDualFreq(arrayListOf(freq1, freq2), arrayListOf(pr1, pr2))
                        }
                    }

                    galPrC = galPr[j] + galCorr

                    //gal GeometricMatrix
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

                        val row = arrayListOf(galAx, galAy, galAz, 1.0)
                        if (isMultiConst) row.add(1.0)
                        galA.add(row)

                    }
                }

                //pseudorange "RAIM"
//                val cleanSatsInd = outliers(galP)
//                cleanSatsInd.forEach {
//                    galP.removeAt(it)
//                    galA.removeAt(it)
//                    galCn0.removeAt(it)
//                }
            }

            //Least Squares
            try {
                if (isMultiConst) {
                    val multiConstP = gpsP + galP
                    val multiconstA = gpsA + galA
                    val multiConstcn0 = gpsCn0 + galCn0

                    responsePvtMultiConst =
                        leastSquares(position, multiConstP, multiconstA, isMultiConst, multiConstcn0, isWeight)
                } else {
                    when {
                        mode.constellations.contains(GPS) -> {
                            responsePvtMultiConst = leastSquares(position, gpsP, gpsA, isMultiConst, gpsCn0, isWeight)
                        }
                        mode.constellations.contains(GALILEO) -> {
                            responsePvtMultiConst = leastSquares(position, galP, galA, isMultiConst, galCn0, isWeight)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d(e.localizedMessage)
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
        val dop = Dop(0.0, 0.0, 0.0)
        var residue = 0.0
        var nSats = 0f
        val corrections = Corrections(0.0, 0.0, 0.0, 0.0, 0.0)
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
        // todo update next ref position
//        acqInformation.refLocation.refLocationEcef =
//            lla2ecef(LlaLocation(pvtLatLng.lat, pvtLatLng.lng, pvtLatLng.altitude))
    }


    return pvtResponsePvtMultiConst
}


@Throws(Exception::class)
fun leastSquares(
    position: PvtEcef,
    arrayPr: List<Double>,
    arrayA: List<ArrayList<Double>>,
    isMultiC: Boolean,
    cnos: List<Double>,
    isWeight: Boolean
): ResponsePvtMultiConst {
    val nSatellites = arrayPr.size
    val nUnknowns = if (isMultiC) 5 else 4
    var response = ResponsePvtMultiConst()
    if (nSatellites >= nUnknowns) {
        if (arrayA.size != nSatellites) {
            Timber.d("A and p are not the same length")
        }

        // Weighted Least Squares: d = inv(G'*W*G)*G'*W*p

        val pVector = SimpleMatrix(nSatellites, 1, true, arrayPr.toDoubleArray())  // Column vector p of nSatellites rows
        val gMatrix = SimpleMatrix(nSatellites, nUnknowns)   // Matrix G of nSatellites rows and nUnknowns columns
        val wMatrix = computeCNoWeightMatrix(cnos, isWeight) // Matrix W of nSatellites rows and nSatellites columns, identity if isWeight = false

        for (row in 0 until arrayA.size){
            gMatrix.set(row, 0, arrayA[row][0])
            gMatrix.set(row, 1, arrayA[row][1])
            gMatrix.set(row, 2, arrayA[row][2])
            gMatrix.set(row, 3, arrayA[row][3])
            if (nUnknowns == 5) gMatrix.set(row, 4, arrayA[row][4])
        }

        // H = G'*W*G
        val hMatrix = (gMatrix.transpose().mult(wMatrix).mult(gMatrix)).invert()
        // d = H*G'*W*p
        val dVector = hMatrix.mult(gMatrix.transpose()).mult(wMatrix).mult(pVector)

        // Residue computation


        // Save results
        position.x += dVector[0, 0]
        position.y += dVector[1, 0]
        position.z += dVector[2, 0]
        position.time = dVector[3, 0]
        if (nUnknowns == 5) position.interSystemBias = dVector[4, 0]

        val pvtEecf = EcefLocation(position.x, position.y, position.z)
        val llaLocation = ecef2lla(pvtEecf)
        val pvtLatLng = PvtLatLng(llaLocation.latitude, llaLocation.longitude, llaLocation.altitude, position.time)

        //todo compute dop and residue
        val dop = Dop()
        val residue = 0.0

        response = ResponsePvtMultiConst(pvtLatLng, dop, residue, Corrections(), nSatellites.toFloat())


//        // PVT computation
//        val daPr = arrayPr.toDoubleArray()
//        var daA = doubleArrayOf()
//        repeat(nSats) { ind ->
//            daA += arrayA[ind]
//        }
//
//        val prMat = DMatrixRMaj.wrap(nSats, 1, daPr)
//        val gMat = DMatrixRMaj.wrap(nSats, nCols, daA)
//
//        // Weight Matrix
//        val wMat = computeCNoWeightMatrix(cnos, isWeight)
//
//        // gwMat = gMat' * wMat
//        var temp = DoubleArray(nCols * nSats)
//        val gwMat = DMatrixRMaj.wrap(nSats, nCols, temp)
//        val gtMat = DMatrixRMaj(gMat)
//        CommonOps_DDRM.transpose(gtMat)
//        CommonOps_DDRM.mult(gtMat, wMat, gwMat)
//
//        // gwgMat = (gMat' * wMat * gMat)
//        val temp2 = DoubleArray(nCols * nCols)
//        val gwgMat = DMatrixRMaj.wrap(nCols, nCols, temp2)
//        CommonOps_DDRM.mult(gwMat, gMat, gwgMat)
//
//        // hMat = inv(gwgMat)
//        val hMat = DMatrixRMaj(gwgMat)
//        CommonOps_DDRM.invert(hMat)
//
//        // hgwMat = hMat*gMat'*wMat
//        val hgwMat = DMatrixRMaj.wrap(nCols, nSats, temp)
//        CommonOps_DDRM.mult(hMat, gwMat, hgwMat)
//
//        // Compute d vector
//        val dMat = DMatrixRMaj.wrap(nCols, 1, temp)
//        CommonOps_DDRM.mult(hgwMat, prMat, dMat)
//
//
//        val dArray = arrayListOf<Double>()
//        repeat(nCols) { i ->
//            dArray.add(dMat[i])
//        }
//
//        // Obtain position
//        position.x += dArray[0]
//        position.y += dArray[1]
//        position.z += dArray[2]
//        position.time = dArray[3] / C
//
//        val pvtEecf = EcefLocation(position.x, position.y, position.z)
//        val llaLocation = ecef2lla(pvtEecf)
//        val pvtLatLng = PvtLatLng(llaLocation.latitude, llaLocation.longitude, llaLocation.altitude, position.time)
//
//        // DOP computation
////        val gDop = sqrt(hgwMat[0, 0] + hgwMat[1, 1] + hgwMat[2, 2] + hgwMat[3, 3])
////        val pDop = sqrt(hgwMat[0, 0] + hgwMat[1, 1] + hgwMat[2, 2])
////        val tDop = sqrt(hgwMat[3, 3])
////        val dop = Dop(gDop, pDop, tDop)
//        val dop = Dop()
//
//        // Residue computation
//        temp = doubleArrayOf()
//        repeat(nSats) {
//            temp += 0.0
//        }
//        val pEstMat = DMatrixRMaj.wrap(nSats, 1, temp)
//        CommonOps_DDRM.mult(gMat, dMat, pEstMat)
//        val resMat = DMatrixRMaj.wrap(nSats, 1, temp)
//        CommonOps_DDRM.subtract(prMat, pEstMat, resMat)
//
//        val residue = sqrt(resMat[0].pow(2) + resMat[1].pow(2) + resMat[2].pow(2))
//
//        response = ResponsePvtMultiConst(pvtLatLng, dop, residue, Corrections(), nSats.toFloat())
    }

    return response
}