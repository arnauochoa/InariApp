package com.inari.team.core.utils.skyplot;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;

/**
 * Interface used by GpsTestActivity to communicate with Gps*Fragments
 */
public interface GnssEventsListener {

    void onGnssStarted();

    void onGnssStopped();

    void onSatelliteStatusChanged(GnssStatus status);

    void onGnssMeasurementsReceived(GnssMeasurementsEvent event);

    void onOrientationChanged(double orientation, double tilt);

    void onNmeaMessageReceived(String message, long timestamp);

    void onLocationReceived(Location location);

}