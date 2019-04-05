package com.inari.team.core.utils

import android.location.GnssMeasurement
import com.inari.team.computation.data.FREQ_THR
import com.inari.team.computation.utils.Constants
import com.inari.team.computation.utils.checkTowDecode
import com.inari.team.presentation.model.GnssData

fun generateMeasurementsTestingLogs(gnssData: GnssData): String {
    var testLog = ""
    val gpsL1List = arrayListOf<GnssMeasurement>()
    val gpsL5List = arrayListOf<GnssMeasurement>()
    val galE1List = arrayListOf<GnssMeasurement>()
    val galE5aList = arrayListOf<GnssMeasurement>()
    gnssData.measurements.forEachIndexed { index, it ->
        it.gnssMeasurements?.forEach {
            when (it.constellationType) {
                Constants.GPS -> {
                    if (!it.hasCarrierFrequencyHz() || it.carrierFrequencyHz >= FREQ_THR) {
                        gpsL1List.add(it)
                    } else {
                        gpsL5List.add(it)
                    }
                }
                Constants.GALILEO -> {
                    if (!it.hasCarrierFrequencyHz() || it.carrierFrequencyHz >= FREQ_THR) {
                        galE1List.add(it)
                    } else {
                        galE5aList.add(it)

                    }
                }
            }
        }

        testLog += "\n\n                                MEASUREMENT $index\n" +
                "--------------------------------------------------------------\n\n"

        gpsL1List.forEach {
            testLog +=
                "GPS L1 -->\n" +
                        "       SVID: ${it.svid}\n" +
                        "       CN0: ${it.cn0DbHz}\n" +
                        "       CHECK_TOW_DECODE: ${checkTowDecode(it.state)}\n" +
                        "       UNCERTAINTY: ${it.receivedSvTimeUncertaintyNanos}\n" +
                        "       MULTIPATH ${it.multipathIndicator}\n" +
                        "---------------------------------------------------\n"
        }

        gpsL5List.forEach {
            testLog +=
                "GPS L5 -->\n" +
                        "       SVID: ${it.svid}\n" +
                        "       CN0: ${it.cn0DbHz}\n" +
                        "       CHECK_TOW_DECODE: ${checkTowDecode(it.state)}\n" +
                        "       UNCERTAINTY: ${it.receivedSvTimeUncertaintyNanos}\n" +
                        "       MULTIPATH ${it.multipathIndicator}\n" +
                        "---------------------------------------------------\n"
        }

        galE1List.forEach {
            testLog +=
                "GAL E1 -->\n" +
                        "       SVID: ${it.svid}\n" +
                        "       CN0: ${it.cn0DbHz}\n" +
                        "       CHECK_TOW_DECODE: ${checkTowDecode(it.state)}\n" +
                        "       UNCERTAINTY: ${it.receivedSvTimeUncertaintyNanos}\n" +
                        "       MULTIPATH ${it.multipathIndicator}\n" +
                        "---------------------------------------------------\n"
        }

        galE5aList.forEach {
            testLog +=
                "GAL E5a -->\n" +
                        "       SVID: ${it.svid}\n" +
                        "       CN0: ${it.cn0DbHz}\n" +
                        "       CHECK_TOW_DECODE: ${checkTowDecode(it.state)}\n" +
                        "       UNCERTAINTY: ${it.receivedSvTimeUncertaintyNanos}\n" +
                        "       MULTIPATH ${it.multipathIndicator}\n" +
                        "---------------------------------------------------\n"
        }
    }


    return testLog
}
