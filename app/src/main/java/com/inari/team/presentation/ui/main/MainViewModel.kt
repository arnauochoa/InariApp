package com.inari.team.presentation.ui.main

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
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.showError
import com.inari.team.core.utils.extensions.showLoading
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.getGnssJson
import com.inari.team.core.utils.saveFile
import com.inari.team.core.utils.savePositionFile
import com.inari.team.presentation.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class MainViewModel @Inject constructor(private val mPrefs: AppSharedPreferences) : BaseViewModel() {

    val position = MutableLiveData<Data<List<ResponsePvtMode>>>()
    val ephemeris = MutableLiveData<Data<String>>()
    val saveLogs = MutableLiveData<Data<Any>>()

    private var gnssData = GnssData()
    private var computedPositions: ArrayList<ResponsePvtMode> = arrayListOf()

    private var suplController: SuplController? = null
    private var refPos: LatLng? = null

    private var startedComputingDate = Date()
    private var isComputing = false
    private var isEphErrorShown = false

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

    fun startComputingPosition(selectedModes: List<Mode>) {
        isEphErrorShown = false
        isComputing = true
        startedComputingDate = Date()
        computedPositions = arrayListOf()

        position.showLoading()
        //init gnss
        gnssData = GnssData()
        gnssData.modes = selectedModes
        gnssData.avg = mPrefs.getAverage()
        gnssData.mask = mPrefs.getSelectedMask()
        gnssData.avgEnabled = mPrefs.isAverageEnabled()

        obtainEphemerisData()
    }

    fun stopComputingPosition() {
        isComputing = false
        ephemeris.updateData("")
    }

    private fun obtainEphemerisData() {
        GlobalScope.launch {
            var ephResponse: EphemerisResponse? = null
            refPos?.let {
                val latE7 = (it.latitude * 1e7).roundToLong()
                val lngE7 = (it.longitude * 1e7).roundToLong()

                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
                suplController?.sendSuplRequest(latE7, lngE7)
                ephResponse = suplController?.generateEphResponse(latE7, lngE7)
                gnssData.ephemerisResponse = ephResponse
                gnssData.lastEphemerisDate = Date()
                ephemeris.updateData("")
            }
            if (ephResponse == null) {
                if (isComputing) {

                    if (Date().time - startedComputingDate.time >=
                        TimeUnit.SECONDS.toMillis(if (gnssData.avgEnabled) mPrefs.getAverage().toLong() else AVG_RATING_DEFAULT)
                    ) {
                        if (!isEphErrorShown) {
                            isEphErrorShown = true
                            ephemeris.showError("")
                        }
                    }
                    obtainEphemerisData()
                }
            }
        }
    }

    //setters
    fun setLocation(location: Location?) {
        location?.let {
            refPos = LatLng(it.latitude, it.longitude)
            gnssData.location = RefLocation(it.latitude, it.longitude, it.altitude)
        }
    }

    fun setGnssStatus(status: GnssStatus?) {
        gnssData.lastGnssStatus = status
    }

    fun setGnssMeasurementsEvent(gnssMeasurementsEvent: GnssMeasurementsEvent?) {
        val measurements = gnssMeasurementsEvent?.measurements
        val clock = gnssMeasurementsEvent?.clock

        measurements?.let {
            if (it.isNotEmpty() && clock != null && gnssData.lastGnssStatus != null) {
                val measurementData = MeasurementData(
                    gnssData.lastGnssStatus,
                    it,
                    clock
                )
                gnssData.measurements.add(measurementData)
            }
        }
        setGnssData()
    }

    //getters
    fun saveLastLogs(fileName: String) {
        val pvtInfoString: String = try {
            getGnssJson(gnssData).toString(2) ?: ""
        } catch (e: JSONException) {
            ""
        }
        val positionsJson = Gson().toJson(computedPositions)
        if (pvtInfoString.isNotBlank()) {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), pvtInfoString))
            savePositionFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), positionsJson))
            saveLogs.updateData("")
        } else {
            saveLogs.showError("")
        }
    }

    //compute position
    private fun setGnssData() {

        if (gnssData.modes.isNotEmpty() &&
            gnssData.location != null &&
            gnssData.measurements.isNotEmpty() &&
            gnssData.ephemerisResponse != null
        ) {

            if (Date().time - startedComputingDate.time >=
                TimeUnit.SECONDS.toMillis(if (gnssData.avgEnabled) mPrefs.getAverage().toLong() else AVG_RATING_DEFAULT)
            ) {
                calculatePositionWithGnss()
            }

            if (Date().time - gnssData.lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                obtainEphemerisData()
            }
        }
    }

    private fun calculatePositionWithGnss() {
        val coordinates = computePosition()

        coordinates?.let {
            position.updateData(it)
            computedPositions.addAll(it)
        } ?: kotlin.run {
            position.showError("Position could not be obtained.")
        }

        gnssData.measurements = arrayListOf()
        startedComputingDate = Date()

    }

    private fun computePosition(): List<ResponsePvtMode>? {
        val responses = arrayListOf<ResponsePvtMode>()

        val jsonGnssData = getGnssJson(gnssData)

        val obtainedPosition = obtainPosition(jsonGnssData.toString(2))

        //todo remove when alfonso changes
        val positionJson = JSONArray(obtainedPosition.substringBeforeLast(",") + "]")

        for (i in 0 until positionJson.length()) {
            positionJson.getJSONObject(i)?.let {
                val latitude = it.get("lat") as? Double
                val longitude = it.get("lng") as? Double
                latitude?.let { lat ->
                    longitude?.let { lon ->
                        responses.add(
                            ResponsePvtMode(LatLng(lat, lon), gnssData.modes[i].color, gnssData.modes[i].name)
                        )
                    }
                }
            }
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