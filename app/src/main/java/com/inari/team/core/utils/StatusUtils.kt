package com.inari.team.core.utils

import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.StatusData
import com.inari.team.presentation.ui.status.StatusFragment
import com.inari.team.presentation.ui.status.StatusFragment.Companion.CONSTELLATION.*
import kotlin.math.floor

private class Satellite(var svid: Int, var constellationId: Int)

fun getStatusData(gnssStatus: GnssStatus, selectedConstellation: StatusFragment.Companion.CONSTELLATION): StatusData {
    val statusData = StatusData()

    statusData.CN0 = getCNoString(gnssStatus, selectedConstellation)
    statusData.satellitesCount = getSatellitesCount(gnssStatus, selectedConstellation)

    return statusData
}

/**
 * Returns the string to be printed on the average CNo field in status views.
 */
fun getCNoString(gnssStatus: GnssStatus, selectedConstellation: StatusFragment.Companion.CONSTELLATION): String {
    var avgCNoString = "--"
    val cnosArrayList = arrayListOf<Float>()

    for (sat in 0 until gnssStatus.satelliteCount) {
        cnosArrayList.add(gnssStatus.getCn0DbHz(sat))
    }

    if (!cnosArrayList.isNullOrEmpty()) {
        val avgCNo = takeTwoDecimals(cnosArrayList.average())
        avgCNoString = "$avgCNo dB-Hz"
    }
    return avgCNoString
}


/**
 * Returns the string to be printed on the number of satellites field in status views.
 */
fun getSatellitesCount(gnssStatus: GnssStatus, selectedConstellation: StatusFragment.Companion.CONSTELLATION): String {
    var satellitesCountString = "--"
    val satelliteArrayList = arrayListOf<Satellite>()

    for (sat in 0 until gnssStatus.satelliteCount) {
        val satellite = Satellite(
            gnssStatus.getSvid(sat),
            gnssStatus.getConstellationType(sat)
        )

        // If satellite (svid-const) is not in satellites list, add it
        if (!satelliteArrayList.any { it.svid == satellite.svid && it.constellationId == satellite.constellationId }) {
            satelliteArrayList.add(satellite)
        }
    }

    if (satelliteArrayList.isNotEmpty()) {
        satellitesCountString = satelliteArrayList.size.toString()
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

fun filterGnssStatus(
    gnssStatus: android.location.GnssStatus,
    selectedConstellation: StatusFragment.Companion.CONSTELLATION
): GnssStatus {

    val filteredGnssStatus: GnssStatus

    val mSvidWithFlags = ArrayList<Int>()
    val mCn0DbHz = ArrayList<Float>()
    val mElevations = ArrayList<Float>()
    val mAzimuths = ArrayList<Float>()
    var mSvCount = 0
    val mCarrierFrequencies = ArrayList<Float>()
    val mConstellationTypes = ArrayList<Int>()

    with(gnssStatus) {

        for (sat in 0 until satelliteCount) {
            if (selectedConstellation.id == ALL.id) {
                if (getConstellationType(sat) == GALILEO.id ||
                    getConstellationType(sat) == GPS.id
                ) {
                    mSvidWithFlags.add(getSvid(sat))
                    mCn0DbHz.add(getCn0DbHz(sat))
                    mElevations.add(getElevationDegrees(sat))
                    mAzimuths.add(getAzimuthDegrees(sat))
                    mSvCount++
                    mCarrierFrequencies.add(getCarrierFrequencyHz(sat))
                    mConstellationTypes.add(getConstellationType(sat))
                }
            } else {
                if (getConstellationType(sat) == selectedConstellation.id) {
                    mSvidWithFlags.add(getSvid(sat))
                    mCn0DbHz.add(getCn0DbHz(sat))
                    mElevations.add(getElevationDegrees(sat))
                    mAzimuths.add(getAzimuthDegrees(sat))
                    mSvCount++
                    mCarrierFrequencies.add(getCarrierFrequencyHz(sat))
                    mConstellationTypes.add(getConstellationType(sat))
                }
            }
        }
    }

    filteredGnssStatus = GnssStatus(
        mSvCount,
        mSvidWithFlags.toIntArray(),
        mCn0DbHz.toFloatArray(),
        mElevations.toFloatArray(),
        mAzimuths.toFloatArray(),
        mCarrierFrequencies.toFloatArray(),
        mConstellationTypes.toIntArray()
    )

    return filteredGnssStatus
}
