package com.inari.team.presentation.ui.position

import android.arch.lifecycle.MutableLiveData
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.StrictMode
import com.google.android.gms.maps.model.LatLng
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.supl.SuplConnectionRequest
import com.google.location.suplclient.supl.SuplController
import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.createDirectory
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.showError
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.obtainJson
import com.inari.team.core.utils.saveFile
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.PositionParameters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PositionViewModel @Inject constructor() : BaseViewModel() {

    val position = MutableLiveData<Data<LatLng>>()

    companion object {
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

    fun setStartTime(avgTime: Long) {
        GlobalScope.launch {
            // Delete previous measurements
            gnssDataJson = JSONArray()
            lastDate = Date()
            this@PositionViewModel.avgTime = avgTime
            try {
                startTimeString = formatter.format(lastDate)
                val directoryName = "$startTimeString/"
                createDirectory(directoryName)
            } catch (e: Exception) {
            }
        }
    }

    fun setGnssData(
        parameters: List<PositionParameters> = arrayListOf(),
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        ephemerisResponse: EphemerisResponse? = null
    ) {

        gnssData.parameters = if (parameters.isNotEmpty()) parameters else gnssData.parameters
        gnssData.location = location ?: gnssData.location
        gnssData.gnssStatus = gnssStatus ?: gnssData.gnssStatus
        gnssData.ephemerisResponse = ephemerisResponse ?: gnssData.ephemerisResponse

        location?.let {
            refPos = LatLng(it.latitude, it.longitude)
        }

        if (gnssData.ephemerisResponse == null) {
            obtainEphemerisData()
        }

        if (gnssData.parameters.isNotEmpty() &&
            gnssData.location != null &&
            gnssData.gnssStatus != null &&
            gnssData.ephemerisResponse != null
        ) {
            gnssMeasurementsEvent?.let {
                gnssData.gnssMeasurements = it.measurements
                gnssData.gnssClock = it.clock
                saveNewGnssData()
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(avgTime)) {
                    calculatePositionWithGnss()
                }
                if (Date().time - lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                    obtainEphemerisData()
                }
            }?: kotlin.run {
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(avgTime)) {
                    calculatePositionWithGnss()
                }
                if (Date().time - lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                    obtainEphemerisData()
                }
            }
        } else {
            position.showError("There is not enough data yet.")
        }
    }

    fun obtainEphemerisData() {
        GlobalScope.launch {
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
            if (ephResponse == null) position.showError("Ephemeris data could not be obtained")
        }
    }

    private fun saveNewGnssData() {
        val mainJson = obtainJson(gnssData, lastEphemerisDate)
        // Testing logs
        //todo remove
        Timber.d(mainJson.toString(2))
        gnssDataJson.put(mainJson)
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
        pvtInfoString?.let {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), it))
        }
    }

    private fun computePosition(): LatLng? {
        var position: LatLng? = null


        //todo remove test positions
        val testPvt = arrayListOf<LatLng>()
        testPvt.add(LatLng(41.4999067, 2.1236877))
        testPvt.add(LatLng(41.4998828, 2.1239955))
        testPvt.add(LatLng(41.4999067, 2.1237777))
        testPvt.add(LatLng(41.4999000, 2.1236877))
        testPvt.add(LatLng(41.4999067, 2.1236878))
        testPvt.add(LatLng(41.4997067, 2.1236874))
        testPvt.add(LatLng(41.4998027, 2.1236872))
        testPvt.add(LatLng(41.4999027, 2.1236873))
        testPvt.add(LatLng(41.4999067, 2.1236874))

        val posIndex = Random().nextInt(9)
        position = testPvt[posIndex]

//        if (gnssDataJson.length() > 0) {
//            val gnssDataString = gnssDataJson.toString(2)
//            val positionJson = JSONObject(obtainPosition(gnssDataString))
//            val latitude = positionJson.get("lat") as Double
//            val longitude = positionJson.get("lng") as Double
//            position = LatLng(latitude, longitude)
//        }
        return position
    }

    /**
     * C++ function used to compute the PVT. This function is defined in Project view modes at the path:
     * /app/src/main/cpp/pvtEngine-lib.cpp
     */
    private external fun obtainPosition(gnssData: String): String


}