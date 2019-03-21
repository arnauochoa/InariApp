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
private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)


fun obtainJson(
    gnssData: GnssData,
    lastEphemerisDate: Date
): JSONObject {

    //TODO REMOVE WHEN ALEJANDRO LE DE LA GANA DE IMPLEMENTAR PARSEAR UNA LISTA DE DATOS
    val firstParameters = gnssData.parameters.isNotEmpty().let { gnssData.parameters[0] }

    val mainJson = JSONObject()
    with(gnssData) {
        mainJson.put(PARAMETERS_KEY, parametersAsJson(firstParameters))
        mainJson.put(LOCATION_KEY, locationAsJson(location))
        mainJson.put(STATUS_KEY, gnssStatusAsJson(gnssStatus))
        mainJson.put(
            MEASUREMENTS_KEY,
            gnssMeasurementsAsJson(gnssMeasurements)
        )
        mainJson.put(CLOCK_KEY, gnssClockAsJson(gnssClock))
        mainJson.put(
            EPHEMERIS_DATA_KEY,
            ephemerisResponseAsJson(ephemerisResponse, lastEphemerisDate)
        )
    }

    return mainJson
}

private fun parametersAsJson(parameters: PositionParameters?): JSONObject {
    var parametersJson = JSONObject()
    parameters?.let {
        parametersJson = it.toJSONObject()
    }
    return parametersJson
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
            val childJson = JSONObject()
            childJson.put("svid", status.getSvid(sat))
            childJson.put("azimuthDegrees", status.getAzimuthDegrees(sat))
            childJson.put("carrierFrequencyHz", status.getCarrierFrequencyHz(sat))
            childJson.put("cn0DbHz", status.getCn0DbHz(sat))
            childJson.put("constellationType", status.getConstellationType(sat))
            childJson.put("elevationDegrees", status.getElevationDegrees(sat))
            childJson.put("hasAlmanacData", status.hasAlmanacData(sat))
            childJson.put("hasCarrierFrequencyHz", status.hasCarrierFrequencyHz(sat))
            childJson.put("hasEphemerisData", status.hasEphemerisData(sat))
            childJson.put("usedInFix", status.usedInFix(sat))

            statusJsonArray.put(childJson)
        }
    }
    return statusJsonArray
}

private fun gnssMeasurementsAsJson(gnssMeasurements: Collection<GnssMeasurement>?): JSONArray {
    val measurementsJsonArray = JSONArray()
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
            childJson.put("hasAutomaticGainControlLevelDb", measurement.hasAutomaticGainControlLevelDb())
            childJson.put("hasCarrierFrequencyHz", measurement.hasCarrierFrequencyHz())
            childJson.put("hasSnrInDb", measurement.hasSnrInDb())

            if (measurement.hasAutomaticGainControlLevelDb()) {
                childJson.put("automaticGainControlLevelDb", measurement.automaticGainControlLevelDb)
            }
            if (measurement.hasCarrierFrequencyHz()) {
                childJson.put("carrierFrequencyHz", measurement.carrierFrequencyHz)
            }
            if (measurement.hasSnrInDb()) {
                childJson.put("snrInDb", measurement.snrInDb)
            }

            measurementsJsonArray.put(childJson)
        }
    }
    return measurementsJsonArray
}

private fun gnssClockAsJson(gnssClock: GnssClock?): JSONObject {
    val clockJson = JSONObject()
    gnssClock?.let { clock ->
        clockJson.put("timeNanos", clock.timeNanos)
        clockJson.put("hardwareClockDiscontinuityCount", clock.hardwareClockDiscontinuityCount)
        clockJson.put("hasBiasNanos", clock.hasBiasNanos())
        clockJson.put("hasBiasUncertaintyNanos", clock.hasBiasUncertaintyNanos())
        clockJson.put("hasDriftNanosPerSecond", clock.hasDriftNanosPerSecond())
        clockJson.put("hasFullBiasNanos", clock.hasFullBiasNanos())
        clockJson.put("hasLeapSecond", clock.hasLeapSecond())
        clockJson.put("hasTimeUncertaintyNanos", clock.hasTimeUncertaintyNanos())
        clockJson.put("hasDriftUncertaintyNanosPerSecond", clock.hasDriftUncertaintyNanosPerSecond())
        if (clock.hasBiasNanos()) {
            clockJson.put("biasNanos", clock.biasNanos)
        }
        if (clock.hasBiasUncertaintyNanos()) {
            clockJson.put("biasUncertaintyNanos", clock.biasUncertaintyNanos)
        }
        if (clock.hasDriftNanosPerSecond()) {
            clockJson.put("driftNanosPerSecond", clock.driftNanosPerSecond)
        }
        if (clock.hasFullBiasNanos()) {
            clockJson.put("fullBiasNanos", clock.fullBiasNanos)
        }
        if (clock.hasLeapSecond()) {
            clockJson.put("leapSecond", clock.leapSecond)
        }
        if (clock.hasTimeUncertaintyNanos()) {
            clockJson.put("timeUncertaintyNanos", clock.timeUncertaintyNanos)
        }
        if (clock.hasDriftUncertaintyNanosPerSecond()) {
            clockJson.put("driftUncertaintyNanosPerSecond", clock.driftUncertaintyNanosPerSecond)
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
        val glonassEphemerisJsonArray = JSONArray()
        it.ephList.forEach { ephemeris ->
            val ephJson = JSONObject(gson.toJson(ephemeris)) // Creates JSONObject of GnssEphemeris object
            when (ephemeris) {
                is GalEphemeris -> galileoEphemerisJsonArray.put(ephJson)
                is GpsEphemeris -> gpsEphemerisJsonArray.put(ephJson)
                is GloEphemeris -> glonassEphemerisJsonArray.put(ephJson)
            }
        }
        ephemerisJson.put(GALILEO_KEY, galileoEphemerisJsonArray)
        ephemerisJson.put(GPS_KEY, gpsEphemerisJsonArray)
        ephemerisJson.put(GLONASS_KEY, glonassEphemerisJsonArray)

        val klobucharJson = JSONObject(gson.toJson(it.ionoProto))
        val neQuickJson = JSONObject(gson.toJson(it.ionoProto2))
        ephemerisJson.put(KLOBUCHAR_KEY, klobucharJson)
        ephemerisJson.put(NEQUICK_KEY, neQuickJson)
    }
    return ephemerisJson
}