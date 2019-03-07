package com.inari.team.ui.position

import android.location.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.ephemeris.GalEphemeris
import com.google.location.suplclient.ephemeris.GloEphemeris
import com.google.location.suplclient.ephemeris.GpsEphemeris
import com.inari.team.data.NavigationMessage
import com.inari.team.data.PositionParameters
import com.inari.team.utils.AppSharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PositionPresenter(private val mView: PositionView?) {

    companion object {
        const val PARAMETERS_KEY = "Params"
        const val LOCATION_KEY = "Location"
        const val STATUS_KEY = "Status"
        const val MEASUREMENTS_KEY = "Meas"
        const val CLOCK_KEY = "Clock"
        const val NAVIGATION_MESSAGES_KEY = "navMsgs"
        const val EPHEMERIS_DATA_KEY = "ephData"
        const val GALILEO_KEY = "Galileo"
        const val GPS_KEY = "GPS"
        const val GLONASS_KEY = "GLONASS"
        const val KLOBUCHAR_KEY = "Klobuchar"
        const val NEQUICK_KEY = "NeQuick"
    }

    private val mSharedPreferences = AppSharedPreferences.getInstance()
    val gson = GsonBuilder().setPrettyPrinting().create()

    private var parameters: PositionParameters? = null
    private var location: Location? = null
    private var gnssStatus: GnssStatus? = null
    private var gnssMeasurements: Collection<GnssMeasurement>? = null
    private var gnssClock: GnssClock? = null
    private var navigationMessages = hashMapOf<Int, NavigationMessage>()
    private var ephemerisResponse: EphemerisResponse? = null

    @RequiresApi(Build.VERSION_CODES.N)
    fun setGnssData(
        parameters: PositionParameters? = null,
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        gnssNavigationMessages: HashMap<Int, NavigationMessage>? = null,
        ephemerisResponse: EphemerisResponse? = null
    ) {

        parameters?.let {
            this.parameters = it
        }

        location?.let {
            this.location = it
        }

        gnssStatus?.let {
            this.gnssStatus = it
        }

        gnssMeasurementsEvent?.let {
            this.gnssMeasurements = it.measurements
            this.gnssClock = it.clock
        }

        gnssNavigationMessages?.let {
            this.navigationMessages = it
        }

        ephemerisResponse?.let {
            this.ephemerisResponse = it
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculatePositionWithGnss() {
        //Calculate the position when parameters are defined and when there are measurements

        val mainJson = obtainJson()

        if (mainJson.isNotEmpty()) {
            // compute position
            val position = computePosition(mainJson)

            // Testing logs
            Log.d("mainJson", mainJson)

            // Saving used data
            mSharedPreferences.deleteData(AppSharedPreferences.PVT_INFO)
            mSharedPreferences.saveData(AppSharedPreferences.PVT_INFO, mainJson)

            if (position != null) {
                mView?.onPositionCalculated(position)
            } else {
                mView?.onPositionNotCalculated()
            }
        } else {
            mView?.onPositionNotCalculated()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtainJson(): String {
        val mainJson = JSONObject()
        //TODO: remove unused jsons
        mainJson.put(PARAMETERS_KEY, parametersAsJson())
        mainJson.put(LOCATION_KEY, locationAsJson())
        mainJson.put(STATUS_KEY, gnssStatusAsJson())
        mainJson.put(MEASUREMENTS_KEY, gnssMeasurementsAsJson())
        mainJson.put(CLOCK_KEY, gnssClockAsJson())
        // mainJson.put(NAVIGATION_MESSAGES_KEY, gnssNavigationMessagesAsJson())
        mainJson.put(EPHEMERIS_DATA_KEY, ephemerisResponseAsJson())

        return mainJson.toString(2)
    }

    private fun computePosition(mainJson: String): LatLng? {
        var position: LatLng? = null
        //TODO: call MATLAB function and transform result to LatLng
        // val latLong = matlabFunction(mainJson)
        // position = LatLng(latLong[0], latLong[1])
        location?.let {
            position = LatLng(it.latitude, it.longitude)
        }
        return position

    }

    private fun parametersAsJson(): JSONObject {
        var parametersJson = JSONObject()
        parameters?.let {
            parametersJson = it.toJSONObject()
        }
        return parametersJson
    }

    private fun locationAsJson(): JSONObject {
        val childJson = JSONObject()
        location?.let { loc ->
            childJson.put("latitude", loc.latitude)
            childJson.put("longitude", loc.longitude)
            childJson.put("altitude", loc.altitude)
        }
        return childJson
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssStatusAsJson(): JSONArray {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssMeasurementsAsJson(): JSONArray {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssClockAsJson(): JSONObject {
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

    private fun gnssNavigationMessagesAsJson(): JSONArray {
        val messagesJsonArray = JSONArray()

        navigationMessages.forEach {
            val childJson = JSONObject()
            childJson.put("svid", it.value.svid)
            childJson.put("type", it.value.type)
            childJson.put("status", it.value.status)
            childJson.put("messageId", it.value.messageId)
            childJson.put("submessageId", it.value.submessageId)
            childJson.put("data", it.value.data?.contentToString())
            messagesJsonArray.put(childJson)
        }
        return messagesJsonArray
    }

    private fun ephemerisResponseAsJson(): JSONObject {
        val gson = Gson()
        val ephemerisJson = JSONObject()
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

}
