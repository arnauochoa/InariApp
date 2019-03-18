package com.inari.team.core.utils

import android.location.GnssStatus
import android.os.Build
import android.support.annotation.RequiresApi
import kotlin.math.floor


/**
 * Returns an ArrayList of the CNo values for the specified constellation. If no constellation is specified,
 * this function returns all the CNo's.
 */
@RequiresApi(Build.VERSION_CODES.N)
fun obtainCNos(gnssStatus: GnssStatus?, constellation: Int? = null): ArrayList<Float> {
    val cnos = arrayListOf<Float>()
    gnssStatus?.let { status ->
        if (constellation == null) { // All constellations
            for (sat in 0 until status.satelliteCount)
                if (status.getConstellationType(sat) == GnssStatus.CONSTELLATION_GPS ||
                    status.getConstellationType(sat) == GnssStatus.CONSTELLATION_GALILEO) {

                    cnos.add(status.getCn0DbHz(sat))
                }
        } else { // Specified constellation
            for (sat in 0 until status.satelliteCount) if (status.getConstellationType(sat) == constellation) {
                cnos.add(status.getCn0DbHz(sat))
            }
        }
    }
    return cnos
}

/**
 * Returns the string to be printed on the average CNo field in status views.
 */
fun getCNoString(cnos: ArrayList<Float>?): String {
    var avgCNoString = "--"
    if (!cnos.isNullOrEmpty()) {
        val avgCNo = takeTwoDecimals(cnos.average())
        avgCNoString = "$avgCNo dB-Hz"
    }
    return avgCNoString
}

/**
 * Returns the string to be printed on the number of satellites field in status views.
 */
fun getSatellitesCount(cnos: ArrayList<Float>?): String {
    var satellitesCountString = "--"

    cnos?.let {
        satellitesCountString = it.size.toString()
    }
    return satellitesCountString
}

/**
 * Returns the given double rounded to two decimals as a string.
 */
private fun takeTwoDecimals(value: Double): String {
    var aux = value * 100
    aux = floor(aux)
    aux /= 100
    return aux.toString()
}
