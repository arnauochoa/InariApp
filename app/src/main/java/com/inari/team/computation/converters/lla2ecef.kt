package com.inari.team.computation.converters

import com.inari.team.computation.data.RefLocationEcef
import com.inari.team.computation.data.RefLocationLla

// WGS84 ellipsoid constants
private const val a = 6378137.0 // radius
private const val e = 8.1819190842622e-2  // eccentricity

private val esq = Math.pow(e, 2.0)

/**
 * Convert geodetic coordinates to Earth-centered Earth-fixed
 */
fun lla2ecef(llaLocation: RefLocationLla): RefLocationEcef {
    var lat = llaLocation.latitude ?: 0.0
    var lon = llaLocation.longitude ?: 0.0
    val alt = llaLocation.altitude ?: 0.0

    lat = Math.toRadians(lat)
    lon = Math.toRadians(lon)


    val n = a / Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2.0))

    val x = (n + alt) * Math.cos(lat) * Math.cos(lon)
    val y = (n + alt) * Math.cos(lat) * Math.sin(lon)
    val z = ((1 - esq) * n + alt) * Math.sin(lat)

    return RefLocationEcef(x, y, z)
}
