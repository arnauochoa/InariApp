package com.inari.team.ui.position

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.inari.team.data.NavigationMessage
import com.inari.team.data.PositionParameters
import com.inari.team.utils.AppSharedPreferences
import org.json.JSONObject

class PositionPresenter(private val mView: PositionView?) {
    private val mSharedPreferences = AppSharedPreferences.getInstance()
    val gson = GsonBuilder().setPrettyPrinting().create()

    private var parameters: PositionParameters? = null
    private var gnssStatus: GnssStatus? = null
    private var gnssMeasurements: Collection<GnssMeasurement>? = null
    private var gnssClock: GnssClock? = null
    private var navigationMessages = hashMapOf<Int, NavigationMessage>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun setGnssData(
        parameters: PositionParameters? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        gnssNavigationMessages: HashMap<Int, NavigationMessage>? = null
    ) {

        parameters?.let {
            this.parameters = it
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


        val parametersJson = parametersAsJson()
        val gnssStatusJson = gnssStatusAsJson()
        val gnssMeasurementsJson = gnssMeasurementsAsJson()
        val gnssClockJson = gnssClockAsJson()
        val gnssNavigationMessageJson = gnssNavigationMessagesAsJson()

        if (parametersJson != null &&
            gnssStatusJson != null &&
            gnssMeasurementsJson != null &&
            gnssClockJson != null &&
            gnssNavigationMessageJson != null
        ) {
            //TODO: Call MATLAB function with the strings previously obtained and params string
            Log.d("parametersJson", parametersJson)
            Log.d("gnssStatusJson", gnssStatusJson)
            Log.d("gnssMeasurementsJson", gnssMeasurementsJson)
            Log.d("gnssClockJson", gnssClockJson)
            Log.d("gnssNavMessageJson", gnssNavigationMessageJson)

            mSharedPreferences.deleteData(AppSharedPreferences.PARAMETERS)
            mSharedPreferences.deleteData(AppSharedPreferences.GNSS_STATUS)
            mSharedPreferences.deleteData(AppSharedPreferences.GNSS_MEASUREMENTS)
            mSharedPreferences.deleteData(AppSharedPreferences.GNSS_CLOCK)
            mSharedPreferences.deleteData(AppSharedPreferences.NAVIGATION_MESSAGES)
            mSharedPreferences.saveData(AppSharedPreferences.PARAMETERS, parametersJson)
            mSharedPreferences.saveData(AppSharedPreferences.GNSS_STATUS, gnssStatusJson)
            mSharedPreferences.saveData(AppSharedPreferences.GNSS_MEASUREMENTS, gnssMeasurementsJson)
            mSharedPreferences.saveData(AppSharedPreferences.GNSS_CLOCK, gnssClockJson)
            mSharedPreferences.saveData(AppSharedPreferences.NAVIGATION_MESSAGES, gnssNavigationMessageJson)

            //once the result is obtained
            //TODO: Transform resulting position to LatLng
            mView?.onPositionCalculated(LatLng(0.0, 0.0))
        }

    }

    private fun parametersAsJson(): String? {
        return if (parameters == null) {
            null
        } else {
            gson.toJson(parameters)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssStatusAsJson(): String? {
        var statusJsonString: String? = null
        gnssStatus?.let { status ->
            val parentJson = JSONObject()
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

                parentJson.put(sat.toString(), childJson)
            }
            statusJsonString = parentJson.toString(2)
        }
        return statusJsonString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssMeasurementsAsJson(): String? {
        var measurementsJsonString: String? = null
        gnssMeasurements?.let { measurements ->
            val parentJson = JSONObject()
            var meas = 0 // measurement index
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

                parentJson.put(meas.toString(), childJson)
                meas++
            }

            measurementsJsonString = parentJson.toString(2)
        }
        return measurementsJsonString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gnssClockAsJson(): String? {
        var clockJsonString: String? = null
        gnssClock?.let { clock ->
            val clockJson = JSONObject()
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
            clockJsonString = clockJson.toString(2)
        }
        return clockJsonString
    }

    private fun gnssNavigationMessagesAsJson(): String? {
        return if (navigationMessages.isEmpty()) {
            null
        } else {
            gson.toJson(navigationMessages)
        }
    }

}