package com.inari.team.computation.infoextractors

import com.inari.team.computation.data.AcqInformation
import com.inari.team.presentation.model.GnssData

fun getAcqInfo(gnssData: GnssData): AcqInformation {

    val acqInformation = AcqInformation()

    gnssData.location?.let { acqInformation.refLocation = it }

    return acqInformation

}