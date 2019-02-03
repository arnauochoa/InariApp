package com.inari.team.ui.status.gps_status

interface GPSStatusView {

    fun onAvgCNoObtained(avgCNo: String)

    fun onSatellitesCountObtained(satellitesCount: String)
}