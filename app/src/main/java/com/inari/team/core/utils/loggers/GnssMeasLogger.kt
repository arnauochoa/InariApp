package com.inari.team.core.utils.loggers

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssMeasurementsEvent
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import com.inari.team.BuildConfig
import java.io.File
import java.io.FileFilter
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GnssMeasLogger {


    companion object {
        const val APP_ROOT = "/Inari/Nmea/Logs/"
        const val FILE_PREFIX = "gnss_log"
        const val MAX_FILES_STORED = 100

        const val COMMENT_START = "# "
        const val VERSION_TAG = "Version: "
    }

    private var mFileWriter: FileWriter? = null

    init {
        startNewLog()
    }

    private fun startNewLog() {

        val baseDirectory = File(Environment.getExternalStorageDirectory(), APP_ROOT)
        baseDirectory.mkdirs()

        val formatter = SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.ENGLISH)
        val now = Date()
        val fileName = String.format("%s_%s.txt", FILE_PREFIX, formatter.format(now))
        val currentFile = File(baseDirectory, fileName)
        try {
            mFileWriter = FileWriter(currentFile)
        } catch (e: IOException) {

        }


        // initialize the contents of the file
        try {
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("Header Description:")
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write(VERSION_TAG)
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val fileVersion = (BuildConfig.VERSION_NAME
                    + " Platform: "
                    + Build.VERSION.RELEASE
                    + " "
                    + "Manufacturer: "
                    + manufacturer
                    + " "
                    + "Model: "
                    + model)
            mFileWriter?.write(fileVersion)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write(
                "Raw,ElapsedRealtimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,FullBiasNanos,"
                        + "BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,"
                        + "HardwareClockDiscontinuityCount,Svid,TimeOffsetNanos,State,ReceivedSvTimeNanos,"
                        + "ReceivedSvTimeUncertaintyNanos,Cn0DbHz,PseudorangeRateMetersPerSecond,"
                        + "PseudorangeRateUncertaintyMetersPerSecond,"
                        + "AccumulatedDeltaRangeState,AccumulatedDeltaRangeMeters,"
                        + "AccumulatedDeltaRangeUncertaintyMeters,CarrierFrequencyHz,CarrierCycles,"
                        + "CarrierPhase,CarrierPhaseUncertainty,MultipathIndicator,SnrInDb,"
                        + "ConstellationType,AgcDb,CarrierFrequencyHz"
            )
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write(
                "Fix,Provider,Latitude,Longitude,Altitude,Speed,Accuracy,(UTC)TimeInMs"
            )
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("Nav,Svid,Type,Status,MessageId,Sub-messageId,Data(Bytes)")
            mFileWriter?.write("\n")
            mFileWriter?.write(COMMENT_START)
            mFileWriter?.write("\n")
        } catch (e: IOException) {
            return
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


    fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent) {
        if (mFileWriter == null) {
            return
        }
        val gnssClock = event.clock
        for (measurement in event.measurements) {
            try {
                writeGnssMeasurementToFile(gnssClock, measurement)
            } catch (e: IOException) {
            }

        }
    }

    @Throws(IOException::class)
    private fun writeGnssMeasurementToFile(clock: GnssClock, measurement: GnssMeasurement) {
        val clockStream = String.format(
            "Raw,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            SystemClock.elapsedRealtime(),
            clock.timeNanos,
            if (clock.hasLeapSecond()) clock.leapSecond else "",
            if (clock.hasTimeUncertaintyNanos()) clock.timeUncertaintyNanos else "",
            clock.fullBiasNanos,
            if (clock.hasBiasNanos()) clock.biasNanos else "",
            if (clock.hasBiasUncertaintyNanos()) clock.biasUncertaintyNanos else "",
            if (clock.hasDriftNanosPerSecond()) clock.driftNanosPerSecond else "",
            if (clock.hasDriftUncertaintyNanosPerSecond())
                clock.driftUncertaintyNanosPerSecond
            else
                "",
            clock.hardwareClockDiscontinuityCount.toString() + ","
        )
        mFileWriter?.write(clockStream)

        val measurementStream = String.format(
            "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            measurement.svid,
            measurement.timeOffsetNanos,
            measurement.state,
            measurement.receivedSvTimeNanos,
            measurement.receivedSvTimeUncertaintyNanos,
            measurement.cn0DbHz,
            measurement.pseudorangeRateMetersPerSecond,
            measurement.pseudorangeRateUncertaintyMetersPerSecond,
            measurement.accumulatedDeltaRangeState,
            measurement.accumulatedDeltaRangeMeters,
            measurement.accumulatedDeltaRangeUncertaintyMeters,
            if (measurement.hasCarrierFrequencyHz()) measurement.carrierFrequencyHz else "",
            if (measurement.hasCarrierCycles()) measurement.carrierCycles else "",
            if (measurement.hasCarrierPhase()) measurement.carrierPhase else "",
            if (measurement.hasCarrierPhaseUncertainty())
                measurement.carrierPhaseUncertainty
            else
                "",
            measurement.multipathIndicator,
            if (measurement.hasSnrInDb()) measurement.snrInDb else "",
            measurement.constellationType,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && measurement.hasAutomaticGainControlLevelDb())
                measurement.automaticGainControlLevelDb
            else
                "",
            if (measurement.hasCarrierFrequencyHz()) measurement.carrierFrequencyHz else ""
        )
        mFileWriter?.write(measurementStream)
        mFileWriter?.write("\n")
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
            } else pathname.length() < PosLogger.MINIMUM_USABLE_FILE_SIZE_BYTES
        }
    }

}