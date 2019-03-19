package com.inari.team.presentation.model

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssStatus
import android.location.Location
import com.google.location.suplclient.ephemeris.EphemerisResponse

data class GnssData(
    var parameters: PositionParameters? = null,
    var location: Location? = null,
    var gnssStatus: GnssStatus? = null,
    var gnssMeasurements: Collection<GnssMeasurement>? = null,
    var gnssClock: GnssClock? = null,
    var ephemerisResponse: EphemerisResponse? = null
)