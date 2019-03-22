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
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.createDirectory
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.showError
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.obtainJson
import com.inari.team.core.utils.saveFile
import com.inari.team.presentation.model.GnssData
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.ResponsePvtMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PositionViewModel @Inject constructor(private val mPrefs: AppSharedPreferences) : BaseViewModel() {

    val position = MutableLiveData<Data<List<ResponsePvtMode>>>()

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

    private var isComputing = false

    private var gnssData = GnssData()

    private var gnssDataJson = JSONArray()

    private var suplController: SuplController? = null
    private var refPos: LatLng? = null
    private var lastEphemerisDate = Date()

    private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)

    var obtainEphemerisIntentsPendingBeforShowError = 2

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

    fun setStartTime() {
        isComputing = true
        GlobalScope.launch {
            // Delete previous measurements
            gnssDataJson = JSONArray()
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
        position.showError(PositionFragment.HIDE_ALERT_ERROR)
    }

    fun setSelectedModes(modes: List<Mode>) {
        gnssData.modes = modes
    }

    fun setLocation(location: Location?) {
        gnssData.location = location
        location?.let {
            refPos = LatLng(it.latitude, it.longitude)
        }
    }

    fun setGnssStatus(status: GnssStatus?) {
        gnssData.gnssStatus = status
    }

    fun setGnssMeasurementsEvent(gnssMeasurementsEvent: GnssMeasurementsEvent?) {
        gnssData.gnssMeasurements = gnssMeasurementsEvent?.measurements
        gnssData.gnssClock = gnssMeasurementsEvent?.clock
        setGnssData()
    }

    private fun setEphemerisResponse(ephemerisResponse: EphemerisResponse?) {
        gnssData.ephemerisResponse = ephemerisResponse
    }

    private fun setGnssData() {

        if (gnssData.modes.isNotEmpty() &&
            gnssData.location != null &&
            gnssData.gnssStatus != null &&
            gnssData.ephemerisResponse != null
        ) {
            gnssData.gnssMeasurements?.isNotEmpty()?.let {
                saveNewGnssData()

                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(mPrefs.getAverage())) {
                    calculatePositionWithGnss()
                }
                if (Date().time - lastEphemerisDate.time >= TimeUnit.HOURS.toMillis(EPHEMERIS_UPDATE_TIME_HOURS)) {
                    obtainEphemerisData()
                }

            }


        }
    }

    fun obtainEphemerisData() {
        obtainEphemerisIntentsPendingBeforShowError--
        GlobalScope.launch {
            var ephResponse: EphemerisResponse? = null
            refPos?.let {
                val latE7 = (it.latitude * 1e7).roundToLong()
                val lngE7 = (it.longitude * 1e7).roundToLong()

                lastEphemerisDate = Date()
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
                suplController?.sendSuplRequest(latE7, lngE7)
                ephResponse = suplController?.generateEphResponse(latE7, lngE7)
                setEphemerisResponse(ephResponse)
                position.showError(PositionFragment.HIDE_ALERT_ERROR)
            }
            if (ephResponse == null) {
                if (isComputing) {
                    if (obtainEphemerisIntentsPendingBeforShowError < 1) {
                        position.showError(PositionFragment.SHOW_ALERT_ERROR)
                    }
                    obtainEphemerisData()
                }
            }
        }
    }

    private fun saveNewGnssData() {
        val mainJson = obtainJson(gnssData, lastEphemerisDate)
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
        if (!pvtInfoString.isNullOrEmpty()) {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), pvtInfoString))
        }
    }

    private fun computePosition(): List<ResponsePvtMode>? {

        val responses = arrayListOf<ResponsePvtMode>()

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

        gnssData.modes.forEachIndexed { _, mode ->
            val posIndex = Random().nextInt(9)
            val position = testPvt[posIndex]
            responses.add(ResponsePvtMode(position, mode.color, mode.name))
        }

//        if (gnssDataJson.length() > 0) {
//            val gnssDataString = gnssDataJson.toString(2)
//            val positionJson = JSONObject(obtainPosition(gnssDataString))
//            val latitude = positionJson.get("lat") as Double
//            val longitude = positionJson.get("lng") as Double
//
//            responses.add(ResponsePvtMode(LatLng(latitude, longitude), getModeColor(0), ""))
//        }

        return responses
    }

    /**
     * C++ function used to compute the PVT. This function is defined in Project view modes at the path:
     * /app/src/main/cpp/pvtEngine-lib.cpp
     */
    private external fun obtainPosition(gnssData: String): String


}