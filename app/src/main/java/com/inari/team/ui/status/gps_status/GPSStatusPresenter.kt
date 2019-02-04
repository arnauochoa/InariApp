package com.inari.team.ui.status.gps_status

import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import com.inari.team.utils.getCNoString
import com.inari.team.utils.getSatellitesCount
import com.inari.team.utils.obtainCNos

class GPSStatusPresenter(private val mView: GPSStatusView?) {

    private var gnssStatus: GnssStatus? = null
    private var gpsCNos = arrayListOf<Float>()

    fun newGnssStatus(gnssStatus: GnssStatus) {
        this.gnssStatus = gnssStatus
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun obtainStatusParameters() {
        gpsCNos = obtainCNos(gnssStatus, GnssStatus.CONSTELLATION_GPS)
        mView?.onSatellitesCountObtained(getSatellitesCount(gpsCNos))
        mView?.onAvgCNoObtained(getCNoString(gpsCNos))
    }
}