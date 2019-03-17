package com.inari.team.core.utils.extensions

import android.location.Location
import com.inari.team.data.api.dto.Coordinate

fun Location.distanceTo(to: Coordinate): Float {
    val loc1 = Location("")
    loc1.latitude = latitude
    loc1.longitude = longitude
    val loc2 = Location("")
    loc2.latitude = to.latitude
    loc2.longitude = to.longitud
    return loc1.distanceTo(loc2)
}