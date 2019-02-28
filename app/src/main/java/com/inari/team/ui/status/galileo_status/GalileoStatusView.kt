package com.inari.team.ui.status.galileo_status

interface GalileoStatusView {

    fun onAvgCNoObtained(avgCNo: String)

    fun onSatellitesCountObtained(satellitesCount: String)
}