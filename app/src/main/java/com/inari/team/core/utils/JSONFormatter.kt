package com.inari.team.core.utils

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssStatus
import com.google.gson.Gson
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.ephemeris.GalEphemeris
import com.google.location.suplclient.ephemeris.GpsEphemeris
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.MeasurementData
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


// JSON keys
const val STATUS_KEY = "Status"
const val MEASUREMENTS_DATA_KEY = "MeasData"
const val MEASUREMENTS_KEY = "Meas"
const val CLOCK_KEY = "Clock"
const val EPHEMERIS_DATA_KEY = "ephData"
const val EPHEMERIS_TIME_KEY = "ephDateTime"
const val GALILEO_KEY = "Galileo"
const val GPS_KEY = "GPS"
const val KLOBUCHAR_KEY = "Klobuchar"
const val NEQUICK_KEY = "NeQuick"

// Others
const val DEFAULT_FREQUENCY_HZ = 1575449984
private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)

fun getGnssJson(gnssData: GnssData): JSONObject {
    val gnssJson = JSONObject(Gson().toJson(gnssData))
    gnssJson.put(MEASUREMENTS_DATA_KEY, gnssMeasurementsListAsJson(gnssData.measurements))
    gnssJson.put(EPHEMERIS_DATA_KEY, ephemerisResponseAsJson(gnssData.ephemerisResponse, Date()))
    return gnssJson
}

fun gnssMeasurementsListAsJson(measurements: List<MeasurementData>): JSONArray {
    val measurementsJsonArray = JSONArray()
    try {
        measurements.forEach {
            val measurementsJson = JSONObject()
            measurementsJson.put(MEASUREMENTS_KEY, gnssMeasurementsAsJson(it.gnssMeasurements))
            measurementsJson.put(CLOCK_KEY, gnssClockAsJson(it.gnssClock))
            measurementsJson.put(STATUS_KEY, gnssStatusAsJsonString(it.gnssStatus))
            measurementsJsonArray.put(measurementsJson)
        }
    } catch (e: Exception) {
    }

    return measurementsJsonArray

}

fun gnssMeasurementsAsJson(gnssMeasurements: Collection<GnssMeasurement>?): JSONArray {
    val measurementsJsonArray = JSONArray()
    try {

        gnssMeasurements?.let { measurements ->
            measurements.forEach { measurement ->
                val childJson = JSONObject()
                childJson.put("svid", measurement.svid)
                childJson.put("constellationType", measurement.constellationType)
                childJson.put("state", measurement.state)
                childJson.put("accumulatedDeltaRangeMeters", measurement.accumulatedDeltaRangeMeters)
                childJson.put("accumulatedDeltaRangeState", measurement.accumulatedDeltaRangeState)
                childJson.put(
                    "accumulatedDeltaRangeUncertaintyMeters",
                    measurement.accumulatedDeltaRangeUncertaintyMeters
                )
                childJson.put("cn0DbHz", measurement.cn0DbHz)
                childJson.put("multipathIndicator", measurement.multipathIndicator)
                childJson.put("pseudorangeRateMetersPerSecond", measurement.pseudorangeRateMetersPerSecond)
                childJson.put(
                    "pseudorangeRateUncertaintyMetersPerSecond",
                    measurement.pseudorangeRateUncertaintyMetersPerSecond
                )
                childJson.put("receivedSvTimeNanos", measurement.receivedSvTimeNanos)
                childJson.put("receivedSvTimeUncertaintyNanos", measurement.receivedSvTimeUncertaintyNanos)
                childJson.put("timeOffsetNanos", measurement.timeOffsetNanos)
                if (measurement.hasAutomaticGainControlLevelDb()) {
                    childJson.put("automaticGainControlLevelDb", measurement.automaticGainControlLevelDb)
                } else {
                    childJson.put("automaticGainControlLevelDb", 0.0)
                }
                if (measurement.hasCarrierFrequencyHz()) {
                    childJson.put("carrierFrequencyHz", measurement.carrierFrequencyHz)
                } else {
                    childJson.put("carrierFrequencyHz", DEFAULT_FREQUENCY_HZ)
                }
                measurementsJsonArray.put(childJson)
            }
        }
    } catch (e: Exception) {
    }
    return measurementsJsonArray
}

fun gnssClockAsJson(gnssClock: GnssClock?): JSONObject {
    val clockJson = JSONObject()

    try {


        gnssClock?.let { clock ->
            clockJson.put("gpsNanos", clock.timeNanos)         // Time nanos
            clockJson.put("hasBiasNanos", clock.hasBiasNanos()) // Boolean flags
            clockJson.put("hasFullBiasNanos", clock.hasFullBiasNanos())
            clockJson.put("hasBiasUncertaintyNanos", clock.hasBiasUncertaintyNanos())
            clockJson.put("hasTimeUncertaintyNanos", clock.hasTimeUncertaintyNanos())
            clockJson.put("hasDriftNanosPerSecond", clock.hasDriftNanosPerSecond())
            clockJson.put("hasDriftUncertaintyNanosPerSecond", clock.hasDriftUncertaintyNanosPerSecond())
            clockJson.put("hasLeapSecond", clock.hasLeapSecond())
            clockJson.put("biasNanos", if (clock.hasBiasNanos()) clock.biasNanos else 0.0) // Possibly empty params
            clockJson.put("fullBiasNanos", if (clock.hasFullBiasNanos()) clock.fullBiasNanos else 0.0)
            clockJson.put(
                "biasUncertaintyNanos",
                if (clock.hasBiasUncertaintyNanos()) clock.biasUncertaintyNanos else 0.0
            )
            clockJson.put(
                "timeUncertaintyNanos",
                if (clock.hasTimeUncertaintyNanos()) clock.timeUncertaintyNanos else 0.0
            )
            clockJson.put(
                "driftNanosPerSecond",
                if (clock.hasDriftNanosPerSecond()) clock.driftNanosPerSecond else 0.0
            )
            clockJson.put(
                "driftUncertaintyNanosPerSecond",
                if (clock.hasDriftUncertaintyNanosPerSecond()) clock.driftUncertaintyNanosPerSecond else 0.0
            )
            clockJson.put(
                "leapSecond",
                if (clock.hasLeapSecond()) clock.leapSecond else 0.0
            )

        }
    } catch (e: Exception) {

    }
    return clockJson
}

fun gnssStatusAsJsonString(gnssStatus: GnssStatus?): JSONArray {
    val statusJsonArray = JSONArray()
    try {


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
    } catch (e: Exception) {

    }
    return statusJsonArray
}


fun ephemerisResponseAsJson(
    ephemerisResponse: EphemerisResponse?,
    lastEphemerisDate: Date?
): JSONObject {
    val gson = Gson()
    val ephemerisJson = JSONObject()
    try {

        ephemerisJson.put(EPHEMERIS_TIME_KEY, formatter.format(lastEphemerisDate))
        ephemerisResponse?.let {
            val galileoEphemerisJsonArray = JSONArray()
            val gpsEphemerisJsonArray = JSONArray()
            it.ephList.forEach { ephemeris ->
                val ephJson = JSONObject(gson.toJson(ephemeris)) // Creates JSONObject of GnssEphemeris object
                when (ephemeris) {
                    is GalEphemeris -> if (isEphemerisValid(ephemeris)) galileoEphemerisJsonArray.put(ephJson)
                    is GpsEphemeris -> if (isEphemerisValid(ephemeris)) gpsEphemerisJsonArray.put(ephJson)
                }
            }
            ephemerisJson.put(GALILEO_KEY, galileoEphemerisJsonArray)
            ephemerisJson.put(GPS_KEY, gpsEphemerisJsonArray)

            val klobucharJson = JSONObject(gson.toJson(it.ionoProto))
            val neQuickJson = JSONObject(gson.toJson(it.ionoProto2))
            ephemerisJson.put(KLOBUCHAR_KEY, klobucharJson)
            ephemerisJson.put(NEQUICK_KEY, neQuickJson)
        }
    } catch (e: Exception) {
    }
    return ephemerisJson
}

/**
 * Check if GalEphemeris object contains all the needed information
 */
fun isEphemerisValid(ephemeris: GalEphemeris): Boolean {
    var validity = true
    if (ephemeris.keplerModel == null) validity = false
    return validity
}

/**
 * Check if GpsEphemeris object contains all the needed information
 */
fun isEphemerisValid(ephemeris: GpsEphemeris): Boolean {
    var validity = true
    if (ephemeris.keplerModel == null) validity = false
    return validity
}














