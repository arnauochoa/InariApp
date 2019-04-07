package com.inari.team.core.utils.loggers

import android.location.GnssStatus
import android.os.Environment
import com.inari.team.computation.data.PvtLatLng
import java.io.File
import java.io.FileFilter
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

        // To make sure that files do not fill up the external storage:
        // - Remove all empty files
        val filter = FileToDeleteFilter(currentFile)
        for (existingFile in baseDirectory.listFiles(filter)!!) {
            existingFile.delete()
        }
        // - Trim the number of files with data
        val existingFiles = baseDirectory.listFiles()
        val filesToDeleteCount = existingFiles!!.size - MAX_FILES_STORED
        if (filesToDeleteCount > 0) {
            Arrays.sort(existingFiles)
            for (i in 0 until filesToDeleteCount) {
                existingFiles[i].delete()
            }
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

        if (constType.isNotBlank()) {
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

    /**
     * Implements a [FileFilter] to delete files that are not in the
     * [FileToDeleteFilter.mRetainedFiles].
     */
    private class FileToDeleteFilter(vararg retainedFiles: File) : FileFilter {
        private val mRetainedFiles: List<File> = Arrays.asList(*retainedFiles)

        /**
         * Returns `true` to delete the file, and `false` to keep the file.
         *
         *
         * Files are deleted if they are not in the [FileToDeleteFilter.mRetainedFiles] list.
         */
        override fun accept(pathname: File?): Boolean {
            if (pathname == null || !pathname.exists()) {
                return false
            }
            return if (mRetainedFiles.contains(pathname)) {
                false
            } else pathname.length() < MINIMUM_USABLE_FILE_SIZE_BYTES
        }
    }

}