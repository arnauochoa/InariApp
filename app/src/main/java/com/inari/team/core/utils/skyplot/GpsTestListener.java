package com.inari.team.core.utils.skyplot;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;

/**
 * Interface used by GpsTestActivity to communicate with Gps*Fragments
 */
public interface GpsTestListener {

    void gpsStart();

    void gpsStop();

    void onGnssStarted();

    void onGnssStopped();

    void onSatelliteStatusChanged(GnssStatus status);

    void onGnssMeasurementsReceived(GnssMeasurementsEvent event);

    void onOrientationChanged(double orientation, double tilt);

}