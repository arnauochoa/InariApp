package com.inari.team.core.utils

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssStatus
import com.google.gson.Gson
import com.google.location.suplclient.ephemeris.GalEphemeris
import com.google.location.suplclient.ephemeris.GpsEphemeris
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.MeasurementData
import org.json.JSONArray
import org.json.JSONObject

// JSON keys
const val STATUS_KEY = "Status"
const val MEASUREMENTS_DATA_KEY = "MeasData"
const val MEASUREMENTS_KEY = "Meas"
const val CLOCK_KEY = "Clock"

// Others
const val DEFAULT_FREQUENCY_HZ = 1575450000

fun getGnssJson(gnssData: GnssData): JSONObject {
    val gnssJson = JSONObject(Gson().toJson(gnssData))
    gnssJson.put(MEASUREMENTS_DATA_KEY, gnssMeasurementsListAsJson(gnssData.measurements))
    return gnssJson
}

fun gnssMeasurementsListAsJson(measurements: List<MeasurementData>): JSONArray {
    val measurementsJsonArray = JSONArray()
    measurements.forEach {
        val measurementsJson = JSONObject()
        measurementsJson.put(MEASUREMENTS_KEY, gnssMeasurementsAsJson(it.gnssMeasurements))
        measurementsJson.put(CLOCK_KEY, gnssClockAsJson(it.gnssClock))
        measurementsJson.put(STATUS_KEY, gnssStatusAsJsonString(it.gnssStatus))
        measurementsJsonArray.put(measurementsJson)
    }

    return measurementsJsonArray

}

fun gnssStatusAsJsonString(gnssStatus: GnssStatus?): JSONArray {
    val statusJsonArray = JSONArray()
    gnssStatus?.let { status ->
        for (sat in 0 until status.satelliteCount) {
            val childJson = JSONObject()
            childJson.put("svid", status.getSvid(sat))
            childJson.put("azimuthDegrees", status.getAzimuthDegrees(sat))
            if (status.hasCarrierFrequencyHz(sat)) {
                childJson.put("carrierFrequencyHz", status.getCarrierFrequencyHz(sat))
            } else {
                childJson.put("carrierFrequencyHz", DEFAULT_FREQUENCY_HZ)
            }
            childJson.put("cn0DbHz", status.getCn0DbHz(sat))
            childJson.put("constellationType", status.getConstellationType(sat))
            childJson.put("elevationDegrees", status.getElevationDegrees(sat))
            statusJsonArray.put(childJson)
        }

    }
    return statusJsonArray
}


fun gnssMeasurementsAsJson(gnssMeasurements: Collection<GnssMeasurement>?): JSONArray {
    val measurementsJsonArray = JSONArray()
    gnssMeasurements?.let { measurements ->
        measurements.forEach { measurement ->
            //            if (measurement.hasCarrierFrequencyHz()) {
            val childJson = JSONObject()
            childJson.put("svid", measurement.svid)
            childJson.put("constellationType", measurement.constellationType)
            childJson.put("accumulatedDeltaRangeMeters", measurement.accumulatedDeltaRangeMeters)
            childJson.put("accumulatedDeltaRangeState", measurement.accumulatedDeltaRangeState)
            childJson.put("cn0DbHz", measurement.cn0DbHz)
            childJson.put("multipathIndicator", measurement.multipathIndicator)
            childJson.put("pseudorangeRateMetersPerSecond", measurement.pseudorangeRateMetersPerSecond)
            childJson.put(
                "pseudorangeRateUncertaintyMetersPerSecond",
                measurement.pseudorangeRateUncertaintyMetersPerSecond
            )
            childJson.put("receivedSvTimeNanos", measurement.receivedSvTimeNanos)
            childJson.put("timeOffsetNanos", measurement.timeOffsetNanos)
            if (measurement.hasCarrierFrequencyHz()) {
                childJson.put("carrierFrequencyHz", measurement.carrierFrequencyHz)
            } else {
                childJson.put("carrierFrequencyHz", DEFAULT_FREQUENCY_HZ)
            }
            measurementsJsonArray.put(childJson)
        }
//        }
    }
    return measurementsJsonArray
}

fun gnssClockAsJson(gnssClock: GnssClock?): JSONObject {
    val clockJson = JSONObject()
    gnssClock?.let { clock ->
        clockJson.put("timeNanos", clock.timeNanos)
        clockJson.put("hasBiasNanos", clock.hasBiasNanos())
        if (clock.hasBiasNanos()) {
            clockJson.put("biasNanos", clock.biasNanos)
        }
        if (clock.hasBiasUncertaintyNanos()) {
            clockJson.put("biasUncertaintyNanos", clock.biasUncertaintyNanos)
        }
        if (clock.hasFullBiasNanos()) {
            clockJson.put("fullBiasNanos", clock.fullBiasNanos)
        }

    }
    return clockJson
}

/**
 * Check if GalEphemeris object contains all the needed information
 */
fun isEphemerisValid(ephemeris: GalEphemeris): Boolean {
    var validity = true
    //TODO: Implement validity check
    return validity
}

/**
 * Check if GpsEphemeris object contains all the needed information
 */
fun isEphemerisValid(ephemeris: GpsEphemeris): Boolean {
    var validity = true
    //TODO: Implement validity check
    return validity
}

