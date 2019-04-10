package com.inari.team.core.utils.loggers

import android.location.GnssStatus
import android.os.Environment
import com.inari.team.computation.data.PvtLatLng
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class PosLogger {


    private var mFileWriter: FileWriter? = null

    companion object {
        const val POS_ROOT = "/Inari/Nmea/Positions/"
        const val FILE_PREFIX = "pos_log"
        const val MAX_FILES_STORED = 100
        const val MINIMUM_USABLE_FILE_SIZE_BYTES = 1000

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

    fun addPositionLine(position: PvtLatLng, nSats: Int, constellations: ArrayList<Int>) {
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

        var NS = 'N'
        var EW = 'E'
        if (position.lat < 0.0)
            NS = 'S'
        if (position.lng < 0.0)
            EW = 'W'

        val locationStream = String.format(
            Locale.ENGLISH,
            "$%sGGA,%s,%s,%c,%s,%c,,%02d,,%s,%c,,%c,,*%s",
            constType,
            position.time,
            convertToNmeaFormat(position.lat),
            NS,
            convertToNmeaFormat(position.lng),
            EW,
            nSats,//todo put size
            if (position.altitude.roundToInt() < 100000) String.format(
                "%05d",
                position.altitude.roundToInt()
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

    fun closeLogger() {
        mFileWriter?.close()
    }

}