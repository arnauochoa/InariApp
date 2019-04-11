package com.inari.team.presentation.ui.main

import android.arch.lifecycle.MutableLiveData
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.StrictMode
import com.google.android.gms.maps.model.LatLng
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.supl.SuplConnectionRequest
import com.google.location.suplclient.supl.SuplController
import com.inari.team.computation.computePvt
import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.showError
import com.inari.team.core.utils.extensions.showLoading
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.loggers.GnssMeasLogger
import com.inari.team.core.utils.loggers.PosLogger
import com.inari.team.presentation.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Singleton
class MainViewModel @Inject constructor(private val mPrefs: AppSharedPreferences) : BaseViewModel() {

    val position = MutableLiveData<Data<List<ResponsePvtMode>>>()
    val ephemeris = MutableLiveData<Data<String>>()

    private var gnssData = GnssData()
    private var computedPositions: ArrayList<ResponsePvtMode> = arrayListOf()

    private var suplController: SuplController? = null
    private var refPos: LatLng? = null

    private var startedComputingDate = Date()
    private var lastMeasurementsDate = Date()

    private var gnssMeasLogger: GnssMeasLogger? = null
    private var posLogger: PosLogger? = null

    private var isComputing = false
    private var isEphErrorShown = false
    private var isFirstComputedPosition = true

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

    fun startComputingPosition(selectedModes: List<Mode>) {
        isEphErrorShown = false
        isComputing = true
        isFirstComputedPosition = true
        startedComputingDate = Date()
        lastMeasurementsDate = Date()
        computedPositions = arrayListOf()

        if (mPrefs.isGnssLoggingEnabled()) {
            gnssMeasLogger = GnssMeasLogger()
            posLogger = PosLogger()
        }

        //init gnss
        gnssData = GnssData()
        gnssData.modes = selectedModes
        gnssData.avg = mPrefs.getAverage()
        gnssData.elevationMask = mPrefs.getSelectedMask()
        gnssData.avgEnabled = mPrefs.isAverageEnabled()
        gnssData.cnoMask = mPrefs.getSelectedCnoMask()
        position.showLoading()

        obtainEphemerisData()
    }

    fun stopComputingPosition() {
        isComputing = false
        gnssMeasLogger?.closeLogger()
        posLogger?.closeLogger()
        gnssMeasLogger = null
        posLogger = null
        ephemeris.updateData("")
    }

    fun clearPositions() {
        computedPositions.clear()
        position.updateData(arrayListOf())
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
            } else ephemeris.updateData("")
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

        if (Date().time - lastMeasurementsDate.time >=
            TimeUnit.SECONDS.toMillis(if (gnssData.avgEnabled) mPrefs.getAverage().toLong() else AVG_RATING_DEFAULT) +
            TimeUnit.SECONDS.toMillis(10)
        ) {
            if (gnssData.measurements.isEmpty()) {
                lastMeasurementsDate = Date()
                position.showError("Measurements are not being obtained")
            }
        }
    }

    fun setGnssMeasurementsEvent(gnssMeasurementsEvent: GnssMeasurementsEvent?) {
        val measurements = gnssMeasurementsEvent?.measurements
        val clock = gnssMeasurementsEvent?.clock

        measurements?.let {
            if (it.isNotEmpty() && clock != null && gnssData.lastGnssStatus != null) {

                gnssMeasLogger?.onGnssMeasurementsReceived(gnssMeasurementsEvent)

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
    fun getComputedPositions(): List<ResponsePvtMode> = computedPositions

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

            if (Date().time - gnssData.lastEphemerisDate.time >= TimeUnit.MINUTES.toMillis(EPHEMERIS_UPDATE_TIME_MINUTES)) {
                obtainEphemerisData()
            }

        }
    }

    private fun calculatePositionWithGnss() {
        GlobalScope.launch {
            val coordinates = computePvt(gnssData)

            if (coordinates.isNotEmpty()) {

                //add position logs
                gnssData.modes.forEach { mode ->
                    coordinates.forEach {
                        posLogger?.addPositionLine(
                            it.pvtLatLng,
                            it.nSatellites.roundToInt(),
                            mode.constellations,
                            it.gpsTime
                        )
                    }
                }

                position.updateData(coordinates)
                computedPositions.addAll(coordinates)
            } else {
                position.showError("Position could not be obtained.")
            }

            gnssData.measurements = arrayListOf()
            startedComputingDate = Date()
        }

    }


    companion object {
        const val SUPL_SERVER_HOST = "supl.google.com"
        const val SUPL_SERVER_PORT = 7275
        const val SUPL_SSL_ENABLED = true
        const val SUPL_MESSAGE_LOGGING_ENABLED = true
        const val SUPL_LOGGING_ENABLED = true
        const val EPHEMERIS_UPDATE_TIME_MINUTES = 5L //min
        const val AVG_RATING_DEFAULT = 1L //s
    }

}