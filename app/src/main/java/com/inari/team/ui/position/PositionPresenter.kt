package com.inari.team.ui.position

import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.StrictMode
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.supl.SuplConnectionRequest
import com.google.location.suplclient.supl.SuplController
import com.inari.team.core.utils.createDirectory
import com.inari.team.core.utils.obtainJson
import com.inari.team.core.utils.saveFile
import com.inari.team.data.GnssData
import com.inari.team.data.PositionParameters
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

class PositionPresenter(private val mView: PositionView?) {

    companion object {
        // Add C++ library
        init {
            System.loadLibrary("pvtEngine-lib")
        }

        const val SUPL_SERVER_HOST = "supl.google.com"
        const val SUPL_SERVER_PORT = 7275
        const val SUPL_SSL_ENABLED = true
        const val SUPL_MESSAGE_LOGGING_ENABLED = true
        const val SUPL_LOGGING_ENABLED = true
        const val EPHEMERIS_UPDATE_TIME_HOURS = 1L
    }

    private var lastDate = Date()
    private var startTimeString: String? = null
    private var avgTime: Long = 5

    private var gnssData = GnssData()

    private var gnssDataJson = JSONArray()

    private var suplController: SuplController? = null
    private var refPos: LatLng? = null
    private var lastEphemerisDate = Date()

    private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)

    init {
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

    fun setStartTime(avgTime: Long) {
        // Delete previous measurements
        gnssDataJson = JSONArray()
        lastDate = Date()
        this.avgTime = avgTime
        try {
            startTimeString = formatter.format(lastDate)
            val directoryName = "$startTimeString/"
            createDirectory(directoryName)
        } catch (e: Exception) {
        }
    }

    fun setGnssData(
        parameters: PositionParameters? = null,
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        ephemerisResponse: EphemerisResponse? = null
    ) {
        gnssData.parameters = parameters ?: gnssData.parameters
        gnssData.location = location ?: gnssData.location
        gnssData.gnssStatus = gnssStatus ?: gnssData.gnssStatus
        gnssData.ephemerisResponse = ephemerisResponse ?: gnssData.ephemerisResponse

        location?.let {
            refPos = LatLng(location.latitude, location.longitude)
        }

        if (gnssData.ephemerisResponse == null) {
            obtainEphemerisData()
        }

        if (gnssData.parameters != null &&
            gnssData.location != null &&
            gnssData.gnssStatus != null &&
            gnssData.ephemerisResponse != null
        ) {
            gnssMeasurementsEvent?.let {
                gnssData.gnssMeasurements = it.measurements
                gnssData.gnssClock = it.clock
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(avgTime)) {
                    calculatePositionWithGnss()
                }
                if (Date().time - lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                    obtainEphemerisData()
                }
                saveNewGnssData()
            }
        } else {
            mView?.showError("There is not enough data yet.")
        }
    }

    fun obtainEphemerisData() {
        var ephResponse: EphemerisResponse? = null
        refPos?.let {
            val latE7 = (it.latitude * 1e7).roundToLong()
            val lngE7 = (it.longitude * 1e7).roundToLong()

            lastEphemerisDate = Date()
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
            suplController?.sendSuplRequest(latE7, lngE7)
            ephResponse = suplController?.generateEphResponse(latE7, lngE7)
            setGnssData(ephemerisResponse = ephResponse)
        }
        if (ephResponse == null) mView?.showError("Ephemeris data could not be obtained")
    }

    private fun saveNewGnssData() {
        val mainJson = obtainJson(gnssData, lastEphemerisDate)
        // Testing logs
        Log.d("mainJson", mainJson.toString(2))
        gnssDataJson.put(mainJson)
    }

    fun calculatePositionWithGnss() {
        //Calculate position and restart averaging
        saveLogsForPostProcessing()
        val position = computePosition()
        gnssDataJson = JSONArray()
        lastDate = Date()

        if (position != null) {
            mView?.onPositionCalculated(position)
        } else {
            mView?.showError("There are not enough measurements yet.")
        }
    }

    //Function used for testing
    private fun saveLogsForPostProcessing() {
        val current = Date()
        val fileName = "$startTimeString/${formatter.format(current)}.txt"
        val pvtInfoString = gnssDataJson.toString(2)
        mView?.showMessage("Saving logs")
        pvtInfoString?.let {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), it))
        }
    }

    private fun computePosition(): LatLng? {
        var position: LatLng? = null

        if (gnssDataJson.length() > 0){
            val gnssDataString = gnssDataJson.toString(2)
            val positionJson = JSONObject(obtainPosition(gnssDataString))
            val latitude = positionJson.get("lat") as Double
            val longitude = positionJson.get("lng") as Double
            position = LatLng(latitude, longitude)
        }
        return position
    }

    external fun obtainPosition(gnssData: String): String

}
