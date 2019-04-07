/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inari.team.core.utils;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import com.inari.team.BuildConfig;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A GNSS logger to store information to a file.
 */
public class GnssMeasurementsFileLogger {

    private static final String FILE_PREFIX = "gnss_log";
    private static final String COMMENT_START = "# ";
    private static final String VERSION_TAG = "Version: ";

    private static final int MAX_FILES_STORED = 100;
    private static final int MINIMUM_USABLE_FILE_SIZE_BYTES = 1000;

    private FileWriter mFileWriter;

    private static final String APP_ROOT = "/Inari/Logs/";

    public GnssMeasurementsFileLogger() {
    }


    /**
     * Start a new file logging process.
     */
    public void startNewLog() {
        File baseDirectory;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            baseDirectory = new File(Environment.getExternalStorageDirectory(), APP_ROOT);
            baseDirectory.mkdirs();
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return;
        } else {
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss", Locale.ENGLISH);
        Date now = new Date();
        String fileName = String.format("%s_%s.txt", FILE_PREFIX, formatter.format(now));
        File currentFile = new File(baseDirectory, fileName);
        try {
            mFileWriter = new FileWriter(currentFile);
        } catch (IOException e) {
            return;
        }

        // initialize the contents of the file
        try {
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("Header Description:");
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write(VERSION_TAG);
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String fileVersion =
                    BuildConfig.VERSION_NAME
                            + " Platform: "
                            + Build.VERSION.RELEASE
                            + " "
                            + "Manufacturer: "
                            + manufacturer
                            + " "
                            + "Model: "
                            + model;
            mFileWriter.write(fileVersion);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write(
                    "Raw,ElapsedRealtimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,FullBiasNanos,"
                            + "BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,"
                            + "HardwareClockDiscontinuityCount,Svid,TimeOffsetNanos,State,ReceivedSvTimeNanos,"
                            + "ReceivedSvTimeUncertaintyNanos,Cn0DbHz,PseudorangeRateMetersPerSecond,"
                            + "PseudorangeRateUncertaintyMetersPerSecond,"
                            + "AccumulatedDeltaRangeState,AccumulatedDeltaRangeMeters,"
                            + "AccumulatedDeltaRangeUncertaintyMeters,CarrierFrequencyHz,CarrierCycles,"
                            + "CarrierPhase,CarrierPhaseUncertainty,MultipathIndicator,SnrInDb,"
                            + "ConstellationType,AgcDb,CarrierFrequencyHz");
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write(
                    "Fix,Provider,Latitude,Longitude,Altitude,Speed,Accuracy,(UTC)TimeInMs");
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("Nav,Svid,Type,Status,MessageId,Sub-messageId,Data(Bytes)");
            mFileWriter.write("\n");
            mFileWriter.write(COMMENT_START);
            mFileWriter.write("\n");
        } catch (IOException e) {
            String exception = e.getLocalizedMessage();
            return;
        }

        if (mFileWriter != null) {
            try {
                mFileWriter.close();
            } catch (IOException e) {

                return;
            }
        }

        // To make sure that files do not fill up the external storage:
        // - Remove all empty files
        FileFilter filter = new FileToDeleteFilter(currentFile);
        for (File existingFile : baseDirectory.listFiles(filter)) {
            existingFile.delete();
        }
        // - Trim the number of files with data
        File[] existingFiles = baseDirectory.listFiles();
        int filesToDeleteCount = existingFiles.length - MAX_FILES_STORED;
        if (filesToDeleteCount > 0) {
            Arrays.sort(existingFiles);
            for (int i = 0; i < filesToDeleteCount; ++i) {
                existingFiles[i].delete();
            }
        }

    }


    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        if (mFileWriter == null) {
            return;
        }
        GnssClock gnssClock = event.getClock();
        for (GnssMeasurement measurement : event.getMeasurements()) {
            try {
                writeGnssMeasurementToFile(gnssClock, measurement);
            } catch (IOException e) {
                String exception = e.getLocalizedMessage();
            }
        }
    }

    private void writeGnssMeasurementToFile(GnssClock clock, GnssMeasurement measurement)
            throws IOException {
        String clockStream =
                String.format(
                        "Raw,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        SystemClock.elapsedRealtime(),
                        clock.getTimeNanos(),
                        clock.hasLeapSecond() ? clock.getLeapSecond() : "",
                        clock.hasTimeUncertaintyNanos() ? clock.getTimeUncertaintyNanos() : "",
                        clock.getFullBiasNanos(),
                        clock.hasBiasNanos() ? clock.getBiasNanos() : "",
                        clock.hasBiasUncertaintyNanos() ? clock.getBiasUncertaintyNanos() : "",
                        clock.hasDriftNanosPerSecond() ? clock.getDriftNanosPerSecond() : "",
                        clock.hasDriftUncertaintyNanosPerSecond()
                                ? clock.getDriftUncertaintyNanosPerSecond()
                                : "",
                        clock.getHardwareClockDiscontinuityCount() + ",");
        mFileWriter.write(clockStream);

        String measurementStream =
                String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        measurement.getSvid(),
                        measurement.getTimeOffsetNanos(),
                        measurement.getState(),
                        measurement.getReceivedSvTimeNanos(),
                        measurement.getReceivedSvTimeUncertaintyNanos(),
                        measurement.getCn0DbHz(),
                        measurement.getPseudorangeRateMetersPerSecond(),
                        measurement.getPseudorangeRateUncertaintyMetersPerSecond(),
                        measurement.getAccumulatedDeltaRangeState(),
                        measurement.getAccumulatedDeltaRangeMeters(),
                        measurement.getAccumulatedDeltaRangeUncertaintyMeters(),
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "",
                        measurement.hasCarrierCycles() ? measurement.getCarrierCycles() : "",
                        measurement.hasCarrierPhase() ? measurement.getCarrierPhase() : "",
                        measurement.hasCarrierPhaseUncertainty()
                                ? measurement.getCarrierPhaseUncertainty()
                                : "",
                        measurement.getMultipathIndicator(),
                        measurement.hasSnrInDb() ? measurement.getSnrInDb() : "",
                        measurement.getConstellationType(),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                && measurement.hasAutomaticGainControlLevelDb()
                                ? measurement.getAutomaticGainControlLevelDb()
                                : "",
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "");
        mFileWriter.write(measurementStream);
        mFileWriter.write("\n");
    }


    /**
     * Implements a {@link FileFilter} to delete files that are not in the
     * {@link FileToDeleteFilter#mRetainedFiles}.
     */
    private static class FileToDeleteFilter implements FileFilter {
        private final List<File> mRetainedFiles;

        public FileToDeleteFilter(File... retainedFiles) {
            this.mRetainedFiles = Arrays.asList(retainedFiles);
        }

        /**
         * Returns {@code true} to delete the file, and {@code false} to keep the file.
         *
         * <p>Files are deleted if they are not in the {@link FileToDeleteFilter#mRetainedFiles} list.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname == null || !pathname.exists()) {
                return false;
            }
            if (mRetainedFiles.contains(pathname)) {
                return false;
            }
            return pathname.length() < MINIMUM_USABLE_FILE_SIZE_BYTES;
        }
    }
}