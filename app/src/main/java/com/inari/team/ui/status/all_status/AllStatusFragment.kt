package com.inari.team.ui.status.all_status

import android.content.Context
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.ui.MainActivity
import com.inari.team.core.utils.skyplot.GpsTestListener
import kotlinx.android.synthetic.main.fragment_all_status.*

class AllStatusFragment : Fragment(), AllStatusView, GpsTestListener {

    private var mPresenter: AllStatusPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = AllStatusPresenter(this)

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        MainActivity.getInstance()?.addListener(this)
    }

    override fun onStatusDataReceived(avgCNo: String, satellitesCount: String) {
        cn0ContentALL.text = avgCNo
        numSatsContentALL.text = satellitesCount
    }

    override fun onSatelliteStatusChanged(status: GnssStatus) {
        mPresenter?.newGnssStatus(status)
        mPresenter?.obtainStatusParameters()
        skyplot.setGnssStatus(status)
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
        skyplot.onOrientationChanged(orientation, tilt)
    }

    override fun onGnssStopped() {
    }

    override fun gpsStart() {
    }

    override fun gpsStop() {
    }

    override fun onGnssStarted() {
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
    }

}
