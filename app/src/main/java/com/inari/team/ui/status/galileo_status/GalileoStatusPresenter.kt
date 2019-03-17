package com.inari.team.ui.status.galileo_status

import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import com.inari.team.core.utils.getCNoString
import com.inari.team.core.utils.getSatellitesCount
import com.inari.team.core.utils.obtainCNos

class GalileoStatusPresenter (private val mView: GalileoStatusView?) {

    private var gnssStatus: GnssStatus? = null
    private var galileoCNos = arrayListOf<Float>()

    fun newGnssStatus(gnssStatus: GnssStatus) {
        this.gnssStatus = gnssStatus
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun obtainStatusParameters() {
        galileoCNos = obtainCNos(gnssStatus, GnssStatus.CONSTELLATION_GALILEO)
        mView?.onSatellitesCountObtained(getSatellitesCount(galileoCNos))
        mView?.onAvgCNoObtained(getCNoString(galileoCNos))
    }
}