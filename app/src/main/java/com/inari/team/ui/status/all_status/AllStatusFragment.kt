package com.inari.team.ui.status.all_status

import android.location.GnssStatus
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import kotlinx.android.synthetic.main.fragment_all_status.*

class AllStatusFragment : Fragment(), AllStatusView {
    private var mPresenter: AllStatusPresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = AllStatusPresenter(this)
    }

    fun onGnssStatusReceived(gnssStatus: GnssStatus) {
        mPresenter?.newGnssStatus(gnssStatus)
        mPresenter?.obtainStatusParameters()
    }

    override fun onStatusDataReceived(avgCNo: String, satellitesCount: String) {
        cn0ContentALL.text = avgCNo
        numSatsContentALL.text = satellitesCount
    }

}
