package com.inari.team.core.utils.loggers;

import android.content.Context;
import android.location.GnssStatus;
import android.util.Log;
import com.inari.team.presentation.model.RefLocation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PositionLogger {



    private static final String FILE_PREFIX = "position_log";
    private static final String ERROR_WRITING_FILE = "Problem writing to file.";
    private static final String COMMENT_START = "# ";
    private static final char RECORD_DELIMITER = ',';
    private static final String VERSION_TAG = "Version: ";

    private static final int MAX_FILES_STORED = 100;
    private static final int MINIMUM_USABLE_FILE_SIZE_BYTES = 1000;

    private final Context mContext;

    private final Object mFileLock = new Object();
    private BufferedWriter mFileWriter;
    private File mFile;


    public PositionLogger(Context context) {
        this.mContext = context;
    }

//    /**
//     * Add new pose to the file
//     */
//    public void addNewPose(RefLocation pose, int constellation) {
//        if (pose != null) {
//
//            synchronized (mFileLock) {
//                if (mFileWriter == null) {
//                    return;
//                }
//                String TalkerID;
//                switch (constellation) {
//                    case GnssStatus.CONSTELLATION_GPS:
//                        TalkerID = "GP";
//                        break;
//                    case GnssStatus.CONSTELLATION_GALILEO:
//                        TalkerID = "GA";
//                        break;
//                    case Constellation.CONSTELLATION_GALILEO_GPS:
//                        TalkerID = "GN";
//                        break;
//                    default:
//                        TalkerID = "";
//                }
//
//                char NS = 'N';
//                char EW = 'E';
//                if (pose.getLatitude() < 0)
//                    NS = 'S';
//                if (pose.getLongitude() < 0)
//                    EW = 'W';
//
//                int GPSQual = 0; // use format %1d
//                int HDOP = 0; // use format %3d
//                int GeoSep = 0; // use format %4d
//                int Checksum = 0; // use format %02x
//                String locationStream =
//                        String.format(Locale.ENGLISH,
//                                "$%sGGA,%s,%s,%c,%s,%c,,%02d,,%s,%c,,%c,,*%s",
//                                TalkerID,
//                                (constellation.getTime() == null) ? "" : constellation.getTime().toLogString(),
//                                convertToNmeaFormat(pose.getLatitude()),
//                                NS,
//                                convertToNmeaFormat(pose.getLongitude()),
//                                EW,
//                                constellation.getUsedConstellationSize(),
//                                ((int) pose.getGeodeticHeight() < 100000) ? String.format("%05d", (int) pose.getGeodeticHeight()) : "100000",
//                                'M',
//                                'M', "");
//                try {
//                    mFileWriter.write(locationStream);
//                    mFileWriter.newLine();
//                } catch (IOException e) {
//                    Log.e("v", ERROR_WRITING_FILE, e);
//                }
//            }
//        }
//    }

}
