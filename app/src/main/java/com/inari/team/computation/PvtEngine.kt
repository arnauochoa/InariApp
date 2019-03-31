package com.inari.team.computation

import com.inari.team.computation.data.AcqInformation
import com.inari.team.computation.infoextractors.getAcqInfo
import com.inari.team.presentation.model.GnssData

fun computePvt(gnssData: GnssData){


    val acqInformation =getAcqInfo(gnssData)


}