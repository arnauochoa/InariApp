package com.inari.team.ui.position

import android.location.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.inari.team.data.NavigationMessage
import com.inari.team.data.PositionParameters
import com.inari.team.utils.AppSharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PositionPresenter(private val mView: PositionView?) {

    companion object {
        const val PARAMETERS_KEY = "parameters"
        const val LOCATION_KEY = "location"
        const val STATUS_KEY = "status"
        const val MEASUREMENTS_KEY = "measurements"
        const val CLOCK_KEY = "clock"
        const val NAVIGATION_MESSAGES_KEY = "navMessage"
    }

    private val mSharedPreferences = AppSharedPreferences.getInstance()
    val gson = GsonBuilder().setPrettyPrinting().create()

    private var parameters: PositionParameters? = null
    private var location: Location? = null
    private var gnssStatus: GnssStatus? = null
    private var gnssMeasurements: Collection<GnssMeasurement>? = null
    private var gnssClock: GnssClock? = null
    private var navigationMessages = hashMapOf<Int, NavigationMessage>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun setGnssData(
        parameters: PositionParameters? = null,
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        gnssNavigationMessages: HashMap<Int, NavigationMessage>? = null
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

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculatePositionWithGnss() {
        //Calculate the position when parameters are defined and when there are measurements

        val mainJson = obtainJson()

        if (mainJson != null) {
            // compute position
            val position = computePosition(mainJson)

            // Testing logs
            Log.d("mainJson", mainJson)

            // Saving used data
            mSharedPreferences.deleteData(AppSharedPreferences.PVT_INFO)
            mSharedPreferences.saveData(AppSharedPreferences.PVT_INFO, mainJson)

            //once the result is obtained
            mView?.onPositionCalculated(position)
        } else {
            mView?.onPositionNotCalculated()
        }

    }

    private fun obtainJson(): String {
        val mainJson = JSONObject()
        mainJson.put(PARAMETERS_KEY, parametersAsJson())
        mainJson.put(LOCATION_KEY, locationAsJson())
        mainJson.put(STATUS_KEY, gnssStatusAsJson())
        mainJson.put(MEASUREMENTS_KEY, gnssMeasurementsAsJson())
        mainJson.put(CLOCK_KEY, gnssClockAsJson())
        mainJson.put(NAVIGATION_MESSAGES_KEY, gnssNavigationMessagesAsJson())

        return mainJson.toString(2)
    }

    private fun computePosition(mainJson: String): LatLng {
        //TODO: call MATLAB function and transform result to LatLng
        return LatLng(0.0, 0.0)
    }

    private fun parametersAsJson(): JSONObject {
        var parametersJson = JSONObject()
        parameters?.let {
            parametersJson = JSONObject(gson.toJson(it))
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

}