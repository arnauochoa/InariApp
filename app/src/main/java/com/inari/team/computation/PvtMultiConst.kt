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
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.PositionParameters
import com.inari.team.presentation.model.PositionParameters.ALG_WLS
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

        val gpsIono = arrayListOf<Pair<Int, Double>>()
        val galIono = arrayListOf<Pair<Int, Double>>()
        var responsePvtMultiConst = ResponsePvtMultiConst()

        var nGps = 0
        var gpsCorr: Double
        var gpsPrC: Double
        var gpsD0: Double
        var gpsAx: Double
        var gpsAy: Double
        var gpsAz: Double
        val gpsA = arrayListOf<ArrayList<Double>>()
        val gpsP = arrayListOf<Double>()
        val gpsTcorr = arrayListOf<Double>()
        var gpsPcorr = arrayListOf<Double>()
        val gpsX = arrayListOf<EcefLocation>()
        val gpsPr = arrayListOf<Double>()
        val gpsSvn = arrayListOf<Int>()
        val gpsCleanSvn = arrayListOf<Int>()
        val gpsCn0 = arrayListOf<Double>()
        val gpsCleanCn0 = arrayListOf<Double>()
        val gpsSatellites = arrayListOf<Satellite>()


        var nGal = 0
        var galCorr: Double = 0.0
        var galPrC: Double = 0.0
        var galD0: Double = 0.0
        var galAx: Double = 0.0
        var galAy: Double = 0.0
        var galAz: Double = 0.0
        val galA = arrayListOf<ArrayList<Double>>()
        val galP = arrayListOf<Double>()
        val galTcorr = arrayListOf<Double>()
        var galPcorr = arrayListOf<Double>()
        val galX = ArrayList<EcefLocation>()
        val galPr = arrayListOf<Double>()
        val galSvn = arrayListOf<Int>()
        val galCleanSvn = arrayListOf<Int>()
        val galCn0 = arrayListOf<Double>()
        val galCleanCn0 = arrayListOf<Double>()
        val galSatellites = arrayListOf<Satellite>()

        repeat(PVT_ITER) { i ->
            //LS

            gpsA.clear()
            gpsP.clear()
            gpsCleanCn0.clear()
            gpsIono.clear()

            galA.clear()
            galP.clear()
            galCleanCn0.clear()
            galIono.clear()

            if (i == 0) {
                //Initialize to the ref position
                position = PvtEcef(
                    acqInformation.refLocation.refLocationEcef.x,
                    acqInformation.refLocation.refLocationEcef.y,
                    acqInformation.refLocation.refLocationEcef.z
                )

                //initialize iono
//                gpsIono = epoch.ionoProto
            }

            //LOOP FOR GPS
            if (mode.constellations.contains(Constants.GPS)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.L1)) {
                        epoch.satellites.gpsSatellites.gpsL1.forEach {
                            gpsPr.add(it.pR)
                            gpsSvn.add(it.svid)
                            gpsCleanSvn.add(it.svid)
                            gpsCn0.add(it.cn0)
                        }
                        nGps = epoch.satellites.gpsSatellites.gpsL1.size
                        gpsSatellites.addAll(epoch.satellites.gpsSatellites.gpsL1)
                    } else {
                        //L5
                        epoch.satellites.gpsSatellites.gpsL5.forEach {
                            gpsPr.add(it.pR)
                            gpsSvn.add(it.svid)
                            gpsCleanSvn.add(it.svid)
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
                    gpsCorr -= propCorr.tropoCorr

                    //2freq corrections
                    if (mode.corrections.contains(PositionParameters.CORR_IONOFREE)) {
                        var pr1 = 0.0
                        var freq1 = 0.0
                        var pr2 = 0.0
                        var freq2 = 0.0

                        epoch.satellites.gpsSatellites.gpsL1.forEach { s ->
                            if (s.svid == gpsCleanSvn[j]) {
                                pr1 = s.pR
                                freq1 = s.carrierFreq
                            }
                        }
                        epoch.satellites.gpsSatellites.gpsL5.forEach { s ->
                            if (s.svid == gpsCleanSvn[j]) {
                                pr2 = s.pR
                                freq2 = s.carrierFreq
                            }
                        }

                        if (pr1 != 0.0 && pr2 != 0.00 && freq1 != 0.0 && freq2 != 0.00) {
                            val ionoCorr = getIonoCorrDualFreq(arrayListOf(freq1, freq2), arrayListOf(pr1, pr2))
                            gpsIono.add(Pair(gpsCleanSvn[j], ionoCorr))
                            gpsCorr -= ionoCorr
                        } else { // If the current satellite has not 2 frequencies, correct with eph iono corrections
                            gpsCorr -= propCorr.ionoCorr
                        }
                    } else { // If iono-free has not been selected, apply eph iono corrections to all satellites
                        gpsCorr -= propCorr.ionoCorr
                    }

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

                        val row = arrayListOf(gpsAx, gpsAy, gpsAz, 1.0)
                        if (isMultiConst) row.add(0.0)
                        gpsA.add(row)
                        gpsCleanCn0.add(gpsCn0[j])
                        gpsCleanSvn.add(gpsSvn[j])
                    }
                }

                //pseudorange "RAIM"
//                val cleanSatsInd = outliers(gpsP)
//                cleanSatsInd.forEach {
//                    gpsP.removeAt(it)
//                    gpsA.removeAt(it)
//                    if (i == 0) gpsCn0.removeAt(it)
//                }

            }

            //LOOP FOR GALILEO
            if (mode.constellations.contains(Constants.GALILEO)) {

                if (i == 0) {
                    if (mode.bands.contains(Constants.E1)) {
                        epoch.satellites.galSatellites.galE1.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCleanSvn.add(it.svid)
                            galCn0.add(it.cn0)
                        }
                        nGal = epoch.satellites.galSatellites.galE1.size
                        galSatellites.addAll(epoch.satellites.galSatellites.galE1)
                    } else {
                        //L5
                        epoch.satellites.galSatellites.galE5a.forEach {
                            galPr.add(it.pR)
                            galSvn.add(it.svid)
                            galCleanSvn.add(it.svid)
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
                    galCorr -= propCorr.tropoCorr

                    //2freq corrections
//                    if (mode.corrections.contains(PositionParameters.CORR_IONOFREE)) {
                    if (true) {
                        var pr1 = 0.0
                        var freq1 = 0.0
                        var pr2 = 0.0
                        var freq2 = 0.0

                        epoch.satellites.galSatellites.galE1.forEach { s ->
                            if (s.svid == galCleanSvn[j]) {
                                pr1 = s.pR
                                freq1 = s.carrierFreq
                            }
                        }
                        epoch.satellites.galSatellites.galE5a.forEach { s ->
                            if (s.svid == galCleanSvn[j]) {
                                pr2 = s.pR
                                freq2 = s.carrierFreq
                            }
                        }

                        if (pr1 != 0.0 && pr2 != 0.00 && freq1 != 0.0 && freq2 != 0.00) {
                            val ionoCorr = getIonoCorrDualFreq(arrayListOf(freq1, freq2), arrayListOf(pr1, pr2))
                            galIono.add(Pair(galCleanSvn[j], ionoCorr))
                            galCorr -= ionoCorr
                        } else {
                            galCorr -= propCorr.ionoCorr
                        }
                    } else {
                        galCorr -= propCorr.ionoCorr
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
                        galCleanCn0.add(galCn0[j])
                        galCleanSvn.add(galSvn[j])
                    }
                }

                // Get rid of pseudorange outliers
//                val cleanSatsInd = outliers(galP)
//                val nUnknowns = if (isMultiConst) 5 else 4
//                if ((galP.size - cleanSatsInd.size) >= nUnknowns) {
//                    cleanSatsInd.forEach {
//                        galP.removeAt(it)
//                        galA.removeAt(it)
//                        galCleanCn0.removeAt(it)
//                    }
//                }
            }

            //Least Squares
            try {
                if (isMultiConst) {
                    val multiConstP = gpsP + galP
                    val multiconstA = gpsA + galA
                    val multiConstcn0 = gpsCleanCn0 + galCleanCn0

                    responsePvtMultiConst =
                        leastSquares(position, multiConstP, multiconstA, isMultiConst, multiConstcn0, isWeight)
                } else {
                    when {
                        mode.constellations.contains(GPS) -> {
                            responsePvtMultiConst =
                                leastSquares(position, gpsP, gpsA, isMultiConst, gpsCleanCn0, isWeight)
                        }
                        mode.constellations.contains(GALILEO) -> {
                            responsePvtMultiConst =
                                leastSquares(position, galP, galA, isMultiConst, galCleanCn0, isWeight)
                            val a = 0
                        }
                    }
                }
            } catch (e: Exception) {
                print("LS ERROR:::${e.localizedMessage}")
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

        val pVector =
            SimpleMatrix(nSatellites, 1, true, arrayPr.toDoubleArray())  // Column vector p of nSatellites rows
        val gMatrix = SimpleMatrix(nSatellites, nUnknowns)   // Matrix G of nSatellites rows and nUnknowns columns
        val wMatrix = computeCNoWeightMatrix(
            cnos,
            isWeight
        ) // Matrix W of nSatellites rows and nSatellites columns, identity if isWeight = false

        for (row in 0 until arrayA.size) {
            gMatrix.set(row, 0, arrayA[row][0])
            gMatrix.set(row, 1, arrayA[row][1])
            gMatrix.set(row, 2, arrayA[row][2])
            gMatrix.set(row, 3, arrayA[row][3])
            if (nUnknowns == 5) gMatrix.set(row, 4, arrayA[row][4])
        }

        // H = inv(G'*W*G)
        var hMatrix = SimpleMatrix(nUnknowns, nUnknowns)
        try {
            hMatrix = gMatrix.transpose().mult(wMatrix).mult(gMatrix).invert()
        } catch (e: Exception) {
            print("LS ERROR:::${e.localizedMessage}")
        }
        hMatrix = gMatrix.transpose().mult(wMatrix).mult(gMatrix).invert()

        // d = inv(H)*G'*W*p
        val dHatVector = hMatrix.mult(gMatrix.transpose()).mult(wMatrix).mult(pVector)

        // W*G matrix
        val wgMatrix = wMatrix.mult(gMatrix)

        // Residue computation: res = p - WG*d
        // todo check this
        val resVector = pVector.minus(wgMatrix.mult(dHatVector))

        // Residue =  |res| = |p - pHat|
        val residue = resVector.normF()

        // DOP computation
        val gDop = sqrt(hMatrix[0, 0] + hMatrix[1, 1] + hMatrix[2, 2] + hMatrix[3, 3])
        val pDop = sqrt(hMatrix[0, 0] + hMatrix[1, 1] + hMatrix[2, 2])
        val tDop = sqrt(hMatrix[3, 3])
        val dop = Dop(gDop, pDop, tDop)

        // Save results
        position.x += dHatVector[0, 0]
        position.y += dHatVector[1, 0]
        position.z += dHatVector[2, 0]
        position.time = dHatVector[3, 0]
        if (nUnknowns == 5) position.interSystemBias = dHatVector[4, 0]

        val pvtEecf = EcefLocation(position.x, position.y, position.z)
        val llaLocation = ecef2lla(pvtEecf)
        val pvtLatLng = PvtLatLng(llaLocation.latitude, llaLocation.longitude, llaLocation.altitude, position.time)

        response = ResponsePvtMultiConst(pvtLatLng, dop, residue, Corrections(), nSatellites.toFloat())

    } else {
        Timber.d("LS ERROR:::not enough satellites")
        print("LS ERROR:::not enough satellites\n")
    }

    return response
}