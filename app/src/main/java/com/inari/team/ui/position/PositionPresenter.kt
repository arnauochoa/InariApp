package com.inari.team.ui.position

import android.location.GnssMeasurementsEvent
import android.location.GnssNavigationMessage
import android.location.GnssStatus
import com.google.android.gms.maps.model.LatLng

class PositionPresenter(private val mView: PositionView?) {
    private var parameters: String? = null

    private var gnssStatus: GnssStatus? = null
    private var gnssMeasurementsEvent: GnssMeasurementsEvent? = null
    private var gnssNavigationMessages = arrayListOf<GnssNavigationMessage>()

    fun setGnssData(
        parameters: String? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        gnssNavigationMessage: GnssNavigationMessage? = null
    ) {

        parameters?.let {
            this.parameters = it
        }

        gnssStatus?.let {
            this.gnssStatus = it
        }

        gnssMeasurementsEvent?.let {
            this.gnssMeasurementsEvent = it
        }

        gnssNavigationMessage?.let {
            this.gnssNavigationMessages.add(it)
            //TODO: Navigation Messages will be constantly received, how to control this?
        }

    }

    fun calculatePositionWithGnss() {
        //Calculate the position when parameters are defined and when there are measurements
        this.parameters?.let { params ->
            this.gnssStatus?.let { status ->
                this.gnssMeasurementsEvent?.let { measurementsEvent ->
                    if (this.gnssNavigationMessages.isNotEmpty()) {

                        var preparedGnssStatus = prepareGnssStatus(status)
                        var preparedGnssMeasurements = prepareGnssMeasurements(measurementsEvent)
                        var preparedGnssNavigationMessage = prepareGnssNavigationMessages(this.gnssNavigationMessages)

                        //TODO: Call MATLAB function with the strings previously obtained and params string

                        //once the result is obtained
                        //TODO: Transform resulting position to LatLng
                        mView?.onPositionCalculated(LatLng(0.0, 0.0))
                    }
                }
            }
        }
    }

    private fun prepareGnssStatus(gnssStatus: GnssStatus): String {
        // TODO: Transform gnssStatus to JSON
        return "gnssStatus as json"
    }

    private fun prepareGnssMeasurements(measurementsEvent: GnssMeasurementsEvent): String {
        // TODO: Transform GnssMeasurements (measurements + clock) to JSON
        return "measurementsEvent as json"
    }

    private fun prepareGnssNavigationMessages(gnssNavigationMessages: ArrayList<GnssNavigationMessage>): String {
        // TODO: Transform GnssNAvigationMessages to JSON
        return "gnssNavigationMessages as json"
    }

}