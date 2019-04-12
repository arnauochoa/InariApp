package com.inari.team.core.utils.loggers

import android.os.Environment
import com.inari.team.computation.data.PvtLatLng
import com.inari.team.computation.utils.GpsTime
import com.inari.team.presentation.model.PositionParameters
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

    private val formatter = SimpleDateFormat("HHmmss.SSS", Locale.ENGLISH)

    init {

        formatter.timeZone = TimeZone.getTimeZone("UTC")

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

    fun addPositionLine(position: PvtLatLng, nSats: Int, constellations: ArrayList<Int>, gpsTime: GpsTime) {
        val constType =
            if (constellations.contains(PositionParameters.CONST_GPS) && constellations.contains(PositionParameters.CONST_GAL)) {
                //multi constellation
                "GN"
            } else {
                when {
                    constellations.contains(PositionParameters.CONST_GPS) -> "GP"
                    constellations.contains(PositionParameters.CONST_GAL) -> "GA"
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
            try {
                formatter.format(gpsTime.getUtcDateTime())
            } catch (e: Exception) {
                "000000.000"
            },
            convertToNmeaFormat(position.lat),
            NS,
            convertToNmeaFormat(position.lng),
            EW,
            nSats,
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
        val geodeticCoordinateMinutes = 60 * (coordinate - geodeticCoordinateDegrees)
        val geodeticCoordinateMinutesInteger = Math.floor(geodeticCoordinateMinutes)
        val geodeticCoordinateMinutesDecimals = geodeticCoordinateMinutes - geodeticCoordinateMinutesInteger

        val mneaFormatCoordinate = StringBuilder()
        val decimalFormat = DecimalFormat("00")
        decimalFormat.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        mneaFormatCoordinate.append(decimalFormat.format(geodeticCoordinateDegrees))
        mneaFormatCoordinate.append(decimalFormat.format(geodeticCoordinateMinutesInteger))
        val decimalFormatSec = DecimalFormat(".######")
        decimalFormatSec.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        mneaFormatCoordinate.append(decimalFormatSec.format(geodeticCoordinateMinutesDecimals))

        return mneaFormatCoordinate.toString()
    }

    fun closeLogger() {
        mFileWriter?.close()
    }

}