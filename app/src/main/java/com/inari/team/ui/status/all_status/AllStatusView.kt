package com.inari.team.ui.status.all_status

interface AllStatusView {

    fun onStatusDataReceived(avgCNo: String, satellitesCount: String)

}