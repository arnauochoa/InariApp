package com.inari.team.presentation.model

import com.google.android.gms.maps.model.LatLng
import com.inari.team.computation.data.PvtLatLng

data class ResponsePvtMode(
    val refPosition: LatLng,
    val refAltitude: Float,
    val pvtLatLng: PvtLatLng,
    val modeColor: Int,
    val modeName: String,
    val nSatellites: Float = 0f,
    var gpsElevIono: ArrayList<Pair<Int, Double>> = arrayListOf(),
    var galElevIono: ArrayList<Pair<Int, Double>> = arrayListOf()
)