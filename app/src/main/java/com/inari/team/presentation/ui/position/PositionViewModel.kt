package com.inari.team.presentation.ui.position

import android.arch.lifecycle.MutableLiveData
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.StrictMode
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.supl.SuplConnectionRequest
import com.google.location.suplclient.supl.SuplController
import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.*
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.showError
import com.inari.team.core.utils.extensions.showLoading
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.MeasurementData
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.ResponsePvtMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PositionViewModel @Inject constructor(private val mPrefs: AppSharedPreferences) : BaseViewModel() {

    val position = MutableLiveData<Data<List<ResponsePvtMode>>>()
    val ephemeris = MutableLiveData<Data<String>>()
    val googlePosition = MutableLiveData<Data<String>>()

    private var lastDate = Date()
    private var startTimeString: String? = null

    private var isComputing = false

    private var gnssData = GnssData()
    private var lastGnssStatus: GnssStatus? = null

    private var gnssDataJson = JSONArray()

    private var suplController: SuplController? = null
    private var refPos: LatLng? = null
    private var lastEphemerisDate = Date()

    private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)

    private var obtainEphemerisIntentsPendingBeforeShowError = 2

    init {
        // Add C++ library
        System.loadLibrary("pvtEngine-lib")
        buildSuplController()
    }

    private fun buildSuplController() {
        val request = SuplConnectionRequest.builder()
            .setServerHost(SUPL_SERVER_HOST)
            .setServerPort(SUPL_SERVER_PORT)
            .setSslEnabled(SUPL_SSL_ENABLED)
            .setMessageLoggingEnabled(SUPL_MESSAGE_LOGGING_ENABLED)
            .setLoggingEnabled(SUPL_LOGGING_ENABLED)
            .build()
        suplController = SuplController(request)
    }

    fun startComputingPosition() {
        isComputing = true
        GlobalScope.launch {
            // Delete previous measurements
            gnssDataJson = JSONArray()
            gnssData.avg = mPrefs.getAverage()
            gnssData.mask = mPrefs.getSelectedMask()
            gnssData.avgEnabled = mPrefs.isAverageEnabled()
            lastDate = Date()
            try {
                startTimeString = formatter.format(lastDate)
                val directoryName = "$startTimeString/"
                createDirectory(directoryName)
            } catch (e: Exception) {
            }
        }
    }

    fun stopComputingPosition() {
        isComputing = false
        ephemeris.updateData(PositionFragment.HIDE_ALERT_ERROR)
    }

    fun obtainEphemerisData() {
        obtainEphemerisIntentsPendingBeforeShowError--
        GlobalScope.launch {
            var ephResponse: EphemerisResponse? = null
            refPos?.let {
                val latE7 = (it.latitude * 1e7).roundToLong()
                val lngE7 = (it.longitude * 1e7).roundToLong()

                lastEphemerisDate = Date()
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
                suplController?.sendSuplRequest(latE7, lngE7)
                ephemeris.updateData(PositionFragment.HIDE_ALERT_ERROR)
                ephResponse = suplController?.generateEphResponse(latE7, lngE7)
                gnssData.ephemerisResponse = ephResponse
            }
            if (ephResponse == null) {
                if (isComputing) {
                    if (obtainEphemerisIntentsPendingBeforeShowError < 1) {
                        ephemeris.showError(PositionFragment.SHOW_ALERT_ERROR)
                    }
                    obtainEphemerisData()
                }
            }
        }
    }

    //setters
    fun setSelectedModes(modes: List<Mode>) {
        gnssData.modes = modes
    }

    fun setLocation(location: Location?) {
        location?.let {
            gnssData.location = LatLng(it.latitude, it.longitude)
            refPos = LatLng(it.latitude, it.longitude)
        }
    }

    fun setGnssStatus(status: GnssStatus?) {
        lastGnssStatus = status
    }

    fun setGnssMeasurementsEvent(gnssMeasurementsEvent: GnssMeasurementsEvent?) {
        val measurements = gnssMeasurementsEvent?.measurements
        val clock = gnssMeasurementsEvent?.clock

        measurements?.let {
            if (it.isNotEmpty() && clock != null && lastGnssStatus != null) {
                val measurementData = MeasurementData(
                    lastGnssStatus,
                    it,
                    clock
                )
                gnssData.measurements.add(measurementData)
            }
        }
        setGnssData()
    }

    //compute position
    private fun setGnssData() {

        if (gnssData.modes.isNotEmpty() &&
            gnssData.location != null &&
            gnssData.measurements.isNotEmpty() &&
            gnssData.ephemerisResponse != null
        ) {

            if (gnssData.avgEnabled) {
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(mPrefs.getAverage().toLong())) {
                    calculatePositionWithGnss()
                }
            } else {
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(AVG_RATING_DEFAULT)) {
                    calculatePositionWithGnss()
                }
            }
            if (Date().time - lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                obtainEphemerisData()
            }
        }
    }

    private fun calculatePositionWithGnss() {
        //Calculate position and restart averaging
        saveLogsForPostProcessing()

        val coordinates = computePosition()

        gnssDataJson = JSONArray()
        lastDate = Date()

        coordinates?.let {
            position.updateData(it)
        } ?: kotlin.run {
            position.showError("There are not enough measurements yet.")
        }

    }

    //Function used for testing
    private fun saveLogsForPostProcessing() {
        val current = Date()
        val fileName = "$startTimeString/${formatter.format(current)}.txt"
        val pvtInfoString = gnssDataJson.toString(2)
        if (!pvtInfoString.isNullOrEmpty()) {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), pvtInfoString))
        }
    }

    private fun computePosition(): List<ResponsePvtMode>? {
        val responses = arrayListOf<ResponsePvtMode>()

        val gnssJson = JSONObject(obtainPosition(Gson().toJson(gnssData)))
        gnssJson.put("MeasData", gnssMeasurementsListAsJson(gnssData.measurements))

        val positionJson = JSONObject(obtainPosition(gnssJson.toString(2)))
        val latitude = positionJson.get("lat") as? Double
        val longitude = positionJson.get("lng") as? Double
        val modeId = positionJson.get("id") as? Int ?: 0
        val modeName = positionJson.get("name") as? String ?: ""

        latitude?.let { lat ->
            longitude?.let { lon ->
                responses.add(ResponsePvtMode(LatLng(lat, lon), getModeColor(modeId), modeName))
            }
        } ?: kotlin.run {
            googlePosition.showLoading()
        }


        return responses
    }

    /**
     * C++ function used to compute the PVT. This function is defined in Project view modes at the path:
     * /app/src/main/cpp/pvtEngine-lib.cpp
     */
    private external fun obtainPosition(gnssData: String): String

    companion object {
        const val SUPL_SERVER_HOST = "supl.google.com"
        const val SUPL_SERVER_PORT = 7275
        const val SUPL_SSL_ENABLED = true
        const val SUPL_MESSAGE_LOGGING_ENABLED = true
        const val SUPL_LOGGING_ENABLED = true
        const val EPHEMERIS_UPDATE_TIME_HOURS = 1L //h
        const val AVG_RATING_DEFAULT = 1L //s
    }


}