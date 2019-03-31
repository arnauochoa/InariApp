package com.inari.team.computation.converters

import com.inari.team.computation.data.EcefLocation
import com.inari.team.computation.data.LlaLocation

// WGS84 ellipsoid constants
private const val a = 6378137.0 // radius
private const val e = 8.1819190842622e-2  // eccentricity

private val asq = Math.pow(a, 2.0)
private val esq = Math.pow(e, 2.0)


/**
 * Convert location object on Earth-centered Earth-fixed to geodetic coordinates
 * WGS84
 */
fun ecef2lla(ecefLocation: EcefLocation): LlaLocation {
    val x = ecefLocation.x
    val y = ecefLocation.y
    val z = ecefLocation.z

    val b = Math.sqrt(asq * (1 - esq))
    val bsq = Math.pow(b, 2.0)
    val ep = Math.sqrt((asq - bsq) / bsq)
    val p = Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0))
    val th = Math.atan2(a * z, b * p)

    var lon = Math.atan2(y, x)
    var lat =
        Math.atan2(z + Math.pow(ep, 2.0) * b * Math.pow(Math.sin(th), 3.0), p - esq * a * Math.pow(Math.cos(th), 3.0))
    val N = a / Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2.0))
    val alt = p / Math.cos(lat) - N

    // mod lat to 0-2pi
    lon %= (2 * Math.PI)

    lat = Math.toDegrees(lat)
    lon = Math.toDegrees(lon)

    return LlaLocation(lat, lon, alt)
}

/**
 * Convert location object on geodetic coordinates to Earth-centered Earth-fixed
 * WGS84
 */
fun lla2ecef(llaLocation: LlaLocation): EcefLocation {
    var lat = llaLocation.latitude ?: 0.0
    var lon = llaLocation.longitude ?: 0.0
    val alt = llaLocation.altitude ?: 0.0

    lat = Math.toRadians(lat)
    lon = Math.toRadians(lon)


    val n = a / Math.sqrt(1 - esq * Math.pow(Math.sin(lat), 2.0))

    val x = (n + alt) * Math.cos(lat) * Math.cos(lon)
    val y = (n + alt) * Math.cos(lat) * Math.sin(lon)
    val z = ((1 - esq) * n + alt) * Math.sin(lat)

    return EcefLocation(x, y, z)
}
