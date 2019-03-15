package com.inari.team.ui.position

import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.inari.team.data.GnssData
import com.inari.team.data.PositionParameters
import com.inari.team.utils.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PositionPresenter(private val mView: PositionView?) {

    private val mSharedPreferences = AppSharedPreferences.getInstance()

    private var lastDate = Date()
    private var startTimeString: String? = null
    private var avgTime: Long = 5

    private var gnssData = GnssData()

    private var previousDataJson = JSONArray()

    private val formatter = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.ENGLISH)

    fun setStartTime(avgTime: Long) {
        // Delete previous measurements
        previousDataJson = JSONArray()
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

        if (gnssData.parameters != null &&
            gnssData.location != null &&
            gnssData.gnssStatus != null &&
            gnssData.ephemerisResponse != null) {
            gnssMeasurementsEvent?.let {
                gnssData.gnssMeasurements = it.measurements
                gnssData.gnssClock = it.clock
                if (Date().time - lastDate.time >= TimeUnit.SECONDS.toMillis(avgTime)) {
                    calculatePositionWithGnss()
                }
                saveNewGnssData()
            }
        } else {
            mView?.showError("There is not enough data yet.")
        }
    }

private fun saveNewGnssData() {
    val mainJson = obtainJson(gnssData)
    // Testing logs
    Log.d("mainJson", mainJson.toString(2))
    previousDataJson.put(mainJson)
}

fun calculatePositionWithGnss() {
    //Calculate position and restart averaging
    saveLogsForPostProcessing()
    val position = computePosition()
    previousDataJson = JSONArray()
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
    val pvtInfoString = previousDataJson.toString(2)
    mView?.showMessage("Saving logs")
    pvtInfoString?.let {
        saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), it))
    }
}

private fun computePosition(): LatLng? {
    var position: LatLng? = null
    val pvtInfoString = mSharedPreferences.getData(AppSharedPreferences.PVT_INFO)
    //TODO: call MATLAB function and transform result to LatLng
    // val latLong = matlabFunction(pvtInfoString)
    // position = LatLng(latLong[0], latLong[1])
    gnssData.location?.let {
        position = LatLng(it.latitude, it.longitude)
    }
    return position
}

}
