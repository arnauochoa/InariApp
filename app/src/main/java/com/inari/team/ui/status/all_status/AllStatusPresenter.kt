package com.inari.team.ui.status.all_status

import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import com.inari.team.utils.getCNoString
import com.inari.team.utils.getSatellitesCount
import com.inari.team.utils.obtainCNos

class AllStatusPresenter(private val mView: AllStatusView?) {

    var gnssStatus: GnssStatus? = null
    private var allCNos = arrayListOf<Float>()

    fun newGnssStatus(gnssStatus: GnssStatus) {
        this.gnssStatus = gnssStatus
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun obtainStatusParameters() {
        allCNos = obtainCNos(gnssStatus)
        mView?.onStatusDataReceived(getCNoString(allCNos), getSatellitesCount(allCNos))
    }
}