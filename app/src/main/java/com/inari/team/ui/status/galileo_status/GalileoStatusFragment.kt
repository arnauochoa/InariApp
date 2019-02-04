package com.inari.team.ui.status.galileo_status

import android.location.GnssStatus
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import kotlinx.android.synthetic.main.fragment_galileo_status.*

class GalileoStatusFragment : Fragment(), GalileoStatusView {
    var mPresenter: GalileoStatusPresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_galileo_status, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = GalileoStatusPresenter(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onGnssStatusReceived(gnssStatus: GnssStatus) {
        mPresenter?.newGnssStatus(gnssStatus)
        mPresenter?.obtainStatusParameters()
    }

    override fun onAvgCNoObtained(avgCNo: String) {
        cn0ContentGAL.text = avgCNo
    }

    override fun onSatellitesCountObtained(satellitesCount: String) {
        numSatsContentGAL.text = satellitesCount
    }

}
