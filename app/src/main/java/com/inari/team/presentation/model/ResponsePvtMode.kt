package com.inari.team.presentation.model

import com.google.android.gms.maps.model.LatLng

data class ResponsePvtMode(
    val refPosition: LatLng,
    val refAltitude: Float,
    val compPosition: LatLng,
    val modeColor: Int,
    val modeName: String,
    val nSatellites: Float = 0f
)