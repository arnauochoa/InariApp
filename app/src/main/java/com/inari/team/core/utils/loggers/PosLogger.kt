package com.inari.team.core.utils.loggers

import android.location.GnssStatus
import android.os.Environment
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class PosLogger {

    private val FILE_PREFIX = "gnss_log"
    private val COMMENT_START = "# "
    private val VERSION_TAG = "Version: "

    private val MAX_FILES_STORED = 100
    private val MINIMUM_USABLE_FILE_SIZE_BYTES = 1000

    private var mFileWriter: FileWriter? = null

    companion object {
        const val POS_ROOT = "/Inari/Positions/"
    }

    init {
        val baseDirectory = File(Environment.getExternalStorageDirectory(), POS_ROOT)
        baseDirectory.mkdirs()

        val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.ENGLISH)
        val now = Date()
        val fileName = String.format("%s_%s.txt", FILE_PREFIX, formatter.format(now))
        val currentFile = File(baseDirectory, fileName)
        try {
            mFileWriter = FileWriter(currentFile)
        } catch (e: IOException) {

        }

    }

    fun addPositionLine(position: LatLng, altitude: Float, time: String, constellations: ArrayList<Int>) {
        val constType =
            if (constellations.contains(GnssStatus.CONSTELLATION_GPS) && constellations.contains(GnssStatus.CONSTELLATION_GALILEO)) {
                //multi constellation
                "GN"
            } else {
                when {
                    constellations.contains(GnssStatus.CONSTELLATION_GPS) -> "GP"
                    constellations.contains(GnssStatus.CONSTELLATION_GALILEO) -> "GA"
                    else -> ""
                }
            }

        if (constType.isNotBlank()) {
            var NS = 'N'
            var EW = 'E'
            if (position.latitude < 0.0)
                NS = 'S'
            if (position.longitude < 0.0)
                EW = 'W'

            val locationStream = String.format(
                Locale.ENGLISH,
                "$%sGGA,%s,%s,%c,%s,%c,,%02d,,%s,%c,,%c,,*%s",
                constType,
                time,
                convertToNmeaFormat(position.latitude),
                NS,
                convertToNmeaFormat(position.longitude),
                EW,
                1,//todo put size
                if (altitude.roundToInt() < 100000) String.format(
                    "%05d",
                    altitude.roundToInt()
                ) else "100000",
                'M',
                'M', ""
            )
            try {
                mFileWriter?.write(locationStream)
                mFileWriter?.write("\n")
            } catch (e: IOException) {
            }
        }

    }

    private fun convertToNmeaFormat(coordinate: Double): String {

        val geodeticCoordinateDegrees = Math.floor(coordinate)
        val geodeticCoordinateMinutes = Math.floor(60 * (coordinate - geodeticCoordinateDegrees))
        val geodeticCoordinateSeconds = 60 * (60 * (coordinate - geodeticCoordinateDegrees) - geodeticCoordinateMinutes)

        val mneaFormatCoordinate = StringBuilder()
        val decimalFormat = DecimalFormat("00")
        decimalFormat.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        mneaFormatCoordinate.append(decimalFormat.format(geodeticCoordinateDegrees))
        mneaFormatCoordinate.append(decimalFormat.format(geodeticCoordinateMinutes))
        val decimalFormatSec = DecimalFormat(".######")
        decimalFormatSec.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        mneaFormatCoordinate.append(decimalFormatSec.format(geodeticCoordinateSeconds / 100))

        return mneaFormatCoordinate.toString();
    }

}