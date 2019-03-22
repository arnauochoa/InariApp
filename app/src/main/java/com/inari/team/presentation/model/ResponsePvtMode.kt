package com.inari.team.presentation.model

import com.google.android.gms.maps.model.LatLng

data class ResponsePvtMode(
    val position: LatLng,
    val modeColor: Int,
    val modeName: String
)