package com.inari.team.core.utils

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssStatus
import android.location.Location
import com.google.gson.Gson
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.ephemeris.GalEphemeris
import com.google.location.suplclient.ephemeris.GloEphemeris
import com.google.location.suplclient.ephemeris.GpsEphemeris
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.PositionParameters
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// JSON keys
const val PARAMETERS_KEY = "Params"
const val LOCATION_KEY = "Location"
const val STATUS_KEY = "Status"
const val MEASUREMENTS_KEY = "Meas"
const val CLOCK_KEY = "Clock"
const val EPHEMERIS_DATA_KEY = "ephData"
const val GALILEO_KEY = "Galileo"
const val GPS_KEY = "GPS"
const val GLONASS_KEY = "GLONASS"
const val KLOBUCHAR_KEY = "Klobuchar"
const val NEQUICK_KEY = "NeQuick"

// Others
const val MAX_TIME_UNCERTAINTY = 1000

private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)


fun obtainJson(
    gnssData: GnssData,
    lastEphemerisDate: Date
): JSONObject {

    val mainJson = JSONObject()
    with(gnssData) {
        val clockJson = gnssClockAsJson(gnssClock)
        if (clockJson.length() > 0) { // If clock measurements has enough information
            mainJson.put(PARAMETERS_KEY, parametersAsJson(parameters))
            mainJson.put(LOCATION_KEY, locationAsJson(location))
            mainJson.put(STATUS_KEY, gnssStatusAsJson(gnssStatus))
            mainJson.put(
                MEASUREMENTS_KEY,
                gnssMeasurementsAsJson(gnssMeasurements)
            )
            mainJson.put(CLOCK_KEY, clockJson)
            mainJson.put(
                EPHEMERIS_DATA_KEY,
                ephemerisResponseAsJson(ephemerisResponse, lastEphemerisDate)
            )
        }
    }

    return mainJson
}

//Todo Remove function if works
private fun parametersAsJson(parameters: List<PositionParameters>): JSONArray {
    val parametersJsonArray = JSONArray()
    parameters.forEach {
        parametersJsonArray.put(it.toJSONObject())
    }
    return parametersJsonArray
}

private fun locationAsJson(location: Location?): JSONObject {
    val childJson = JSONObject()
    location?.let { loc ->
        childJson.put("latitude", loc.latitude)
        childJson.put("longitude", loc.longitude)
        childJson.put("altitude", loc.altitude)
    }
    return childJson
}

private fun gnssStatusAsJson(gnssStatus: GnssStatus?): JSONArray {
    val statusJsonArray = JSONArray()
    gnssStatus?.let { status ->
        for (sat in 0 until status.satelliteCount) {
            if (status.hasCarrierFrequencyHz(sat)) {
                val childJson = JSONObject()
                childJson.put("svid", status.getSvid(sat))
                childJson.put("azimuthDegrees", status.getAzimuthDegrees(sat))
                childJson.put("carrierFrequencyHz", status.getCarrierFrequencyHz(sat))
                childJson.put("cn0DbHz", status.getCn0DbHz(sat))
                childJson.put("constellationType", status.getConstellationType(sat))
                childJson.put("elevationDegrees", status.getElevationDegrees(sat))
                statusJsonArray.put(childJson)
            }
        }
    }
    return statusJsonArray
}

private fun gnssMeasurementsAsJson(gnssMeasurements: Collection<GnssMeasurement>?): JSONArray {
    val measurementsJsonArray = JSONArray()
    gnssMeasurements?.let { measurements ->
        measurements.forEach { measurement ->
            if (measurement.hasCarrierFrequencyHz() &&
                measurement.receivedSvTimeUncertaintyNanos < MAX_TIME_UNCERTAINTY) {
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
                childJson.put("carrierFrequencyHz", measurement.carrierFrequencyHz)
                measurementsJsonArray.put(childJson)
            }
        }
    }
    return measurementsJsonArray
}

private fun gnssClockAsJson(gnssClock: GnssClock?): JSONObject {
    val clockJson = JSONObject()
    gnssClock?.let { clock ->
        if (clock.hasBiasNanos() && clock.hasBiasUncertaintyNanos() && clock.hasFullBiasNanos()) {
            clockJson.put("timeNanos", clock.timeNanos)
            clockJson.put("biasNanos", clock.biasNanos)
            clockJson.put("biasUncertaintyNanos", clock.biasUncertaintyNanos)
            clockJson.put("fullBiasNanos", clock.fullBiasNanos)
        }
    }
    return clockJson
}

private fun ephemerisResponseAsJson(
    ephemerisResponse: EphemerisResponse?,
    lastEphemerisDate: Date
): JSONObject {
    val gson = Gson()
    val ephemerisJson = JSONObject()
    ephemerisJson.put("Time: ", formatter.format(lastEphemerisDate))
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
    return ephemerisJson
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
    return validity}

