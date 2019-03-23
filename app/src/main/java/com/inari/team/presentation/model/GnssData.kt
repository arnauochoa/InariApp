package com.inari.team.presentation.model

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssStatus
import com.google.android.gms.maps.model.LatLng
import com.google.location.suplclient.ephemeris.EphemerisResponse

data class GnssData(
    var modes: List<Mode> = arrayListOf(),
    var location: LatLng? = null,
    var ephemerisResponse: EphemerisResponse? = null,
    @Transient var measurements: ArrayList<MeasurementData> = arrayListOf(),
    var avgEnabled: Boolean = false,
    var avg: Int = 5,
    var mask: Int = 10
)

data class MeasurementData(
    var gnssStatus: GnssStatus? = null,
    var gnssMeasurements: Collection<GnssMeasurement>? = null,
    var gnssClock: GnssClock? = null
)