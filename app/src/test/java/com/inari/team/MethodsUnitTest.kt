package com.inari.team

import com.google.location.suplclient.ephemeris.KeplerianModel
import com.inari.team.computation.converters.Topocentric
import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.converters.lla2ecef
import com.inari.team.computation.converters.toGeod
import com.inari.team.computation.corrections.getCtrlCorr
import com.inari.team.computation.corrections.ionoErrorCorrections
import com.inari.team.computation.corrections.klobucharModel
import com.inari.team.computation.corrections.tropoErrorCorrection
import com.inari.team.computation.data.LlaLocation
import com.inari.team.computation.data.Satellite
import com.inari.team.computation.satPos
import com.inari.team.computation.utils.*
import org.junit.Test

class MethodsUnitTest {

    @Test
    fun methods_areCorrect() {
        //test lla2ecef
        val llaLocation = LlaLocation(42.2383729097, 19.3774822039, 100.0)
        val llaLocation2 = LlaLocation(-53.9828324342, -2.3774822039, 1200.0)
        val ecefLocation = lla2ecef(llaLocation)
        val ecefLocation2 = lla2ecef(llaLocation2)
        val llaLocationRec = ecef2lla(ecefLocation)
        val llaLocation2Rec = ecef2lla(ecefLocation2)

        //test mod
        val num = applyMod(5.2, 3)

        //test check state
        val t1 = checkTowDecode(0)
        val t2 = checkTowKnown(0)
        val t3 = checkGalState(0)

        val x = doubleArrayOf(50000.0, 30000.0, 60000.0)
        val tgd = toGeod(6378137.0, 298.257223563, x[0], x[1], x[2])
        val dx = doubleArrayOf(200.0, 110.0, 521.0)
//        val topocent = toTopocent(x, dx)

        val time1 = checkTime(-302420.0)
        val time2 = checkTime(-3024.0)
        val time3 = checkTime(3020.0)
        val time4 = checkTime(302520.0)

        val xSatRot = earthRotCorr(0.5, doubleArrayOf(8000.0, 7000.0, 5000.0))

        val gpst = nsgpst2gpst(62968924186776)

        val satPos = satPos(
            1.3933e5, Satellite(
                1,
                2,
                3,
                4.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9,
                10,
                7200.0,
                2057,
                0.0,
                0.0,
                15.0,
                16.0,
                keplerModel = KeplerianModel(
                    KeplerianModel.newBuilder()
                        .setCic(2.533197402954102e-07)
                        .setCis(-3.352761268615723e-08)
                        .setCrc(1.889687500000000e+02)
                        .setCrs(-23.031250000000000)
                        .setCuc(-1.097097992897034e-06)
                        .setCus(9.521842002868652e-06)
                        .setDeltaN(4.345538151963753e-09)
                        .setEccentricity(0.018924818490632)
                        .setI0(0.953742579229515)
                        .setIDot(4.253748614275359e-10)
                        .setM0(-2.851940672902162)
                        .setOmega(-1.756671707742028)
                        .setOmega0(-1.918848428767706)
                        .setOmegaDot(-7.638889618985842e-09)
                        .setSqrtA(5.153678335189819e+03)
                        .setToeS(144000.0)
                )

            ), Constants.GPS
        )

        val ctrlCorr = getCtrlCorr(
            Satellite(
                1,
                2,
                3,
                4.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9,
                10,
                144000.0,
                2057,
                -1.724963076412678e-4,
                -9.549694368615746e-12,
                0.0,
                -2.048909664154053e-8,
                keplerModel = KeplerianModel(
                    KeplerianModel.newBuilder()
                        .setCic(2.533197402954102e-07)
                        .setCis(-3.352761268615723e-08)
                        .setCrc(1.889687500000000e+02)
                        .setCrs(-23.031250000000000)
                        .setCuc(-1.097097992897034e-06)
                        .setCus(9.521842002868652e-06)
                        .setDeltaN(4.345538151963753e-09)
                        .setEccentricity(0.018924818490632)
                        .setI0(0.953742579229515)
                        .setIDot(4.253748614275359e-10)
                        .setM0(-2.851940672902162)
                        .setOmega(-1.756671707742028)
                        .setOmega0(-1.918848428767706)
                        .setOmegaDot(-7.638889618985842e-09)
                        .setSqrtA(5.153678335189819e+03)
                        .setToeS(144000.0)
                )

            ), 139333.0, 2.339905407194209e7, Constants.GPS
        )

        val klobuchar =
            klobucharModel(
                41.499659208446790,
                2.111580977531347,
                63.227595211119020,
                41.093229197678930,
                139333.0,
                arrayListOf(
                    1.117587089538574e-08,
                    7.450580596923828e-09,
                    -5.960464477539063e-08,
                    -5.960464477539063e-08,
                    90112.0,
                    16384.0,
                    -196608.0,
                    -65536.0
                )
            )

        val ionoCorr = ionoErrorCorrections(
            LlaLocation(41.499659208446790, 2.111580977531347, 0.0),
            Topocentric(63.227595211119020, 41.093229197678930, 0.0),
            139333.0,
            arrayListOf(
                1.117587089538574e-08,
                7.450580596923828e-09,
                -5.960464477539063e-08,
                -5.960464477539063e-08,
                90112.0,
                16384.0,
                -196608.0,
                -65536.0
            ),
            Constants.KLOBUCHAR
        )

        val torpoErr = tropoErrorCorrection(arrayListOf(41.093229197678930), arrayListOf(1.834025183355422e+02))

        val wMat = computeCNoWeightMatrix(
            arrayListOf(
                27.267833709716797,
                32.965755462646484,
                39.137847900390625,
                35.135646820068359,
                40.287666320800781,
                37.656314849853516,
                30.272438049316406,
                27.956821441650391,
                26.901241302490234,
                34.949981689453125,
                28.495033264160156,
                17.849727630615234
            ), true
        )

        print(wMat)



    }
}