package com.inari.team.ui.status.allStatus

import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import kotlin.math.floor

class AllStatusPresenter(private val mView: AllStatusView?) {

    var gnssStatus: GnssStatus? = null

    fun newGnssStatus(gnssStatus: GnssStatus) {
        this.gnssStatus = gnssStatus
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun obtainStatusParameters() {
        mView?.onSatellitesCountObtained(getSatellitesCount())
        mView?.onAvgCNoObtained(getCnoString())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getSatellitesCount(): String {
        var satellitesCountString = "--"

        gnssStatus?.let {
            satellitesCountString = it.satelliteCount.toString()
        }

        return satellitesCountString
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCnoString(): String {
        var avgCNoString = "--"

        gnssStatus?.let {
            val allCNos = arrayListOf<Float>()
            for (sat in 0 until it.satelliteCount) {
                allCNos.add(it.getCn0DbHz(sat))
            }
            val avgCNo = takeTwoDecimals(allCNos.average())
            avgCNoString = "$avgCNo dB-Hz"
        }

        return avgCNoString
    }

    private fun takeTwoDecimals(value: Double): String {
        var aux = value * 100
        aux = floor(aux)
        aux /= 100
        return aux.toString()
    }


}