package com.inari.team.ui.status


import android.graphics.Color
import android.location.GnssMeasurementsEvent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.DataState.*
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.filterGnssStatus
import com.inari.team.core.utils.skyplot.GpsTestListener
import com.inari.team.data.GnssStatus
import com.inari.team.data.StatusData
import com.inari.team.ui.status.StatusFragment.Companion.CONSTELLATION.ALL
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : BaseFragment(), GpsTestListener {


    companion object {
        enum class CONSTELLATION(var id: Int) {
            ALL(-1), GALILEO(GnssStatus.CONSTELLATION_GALILEO), GPS(GnssStatus.CONSTELLATION_GPS)
        }
    }

    private var viewModel: StatusViewModel? = null

    private var selectedConstellation: CONSTELLATION = ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        viewModel = withViewModel(viewModelFactory) {
            observe(status, ::updateStatusData)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFilterTabs()
    }

    override fun onResume() {
        super.onResume()
        tabLayout?.getTabAt(0)?.select()
    }

    private fun setFilterTabs() {

        tvLegend.setOnClickListener {
            clLegend.visibility = if (clLegend.visibility == VISIBLE) {
                GONE
            } else {
                VISIBLE
            }
        }

        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)

        CONSTELLATION.values().forEach {
            tabLayout.addTab(createTab(it.name))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(R.drawable.bg_corners_red)
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(R.drawable.bg_corners_gray)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(R.drawable.bg_corners_red)
                tab?.position?.let {
                    if (it < CONSTELLATION.values().size) {
                        selectedConstellation = CONSTELLATION.values()[it]
                    }
                }
            }

        })

    }

    //helpers
    private fun createTab(title: String): TabLayout.Tab {
        val tab = tabLayout.newTab().setCustomView(R.layout.item_filter_tab)
        tab.customView?.findViewById<TextView>(R.id.tvFilterTitle)?.text = title
        return tab
    }

    //callbacks
    private fun updateStatusData(data: Data<StatusData>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                }
                SUCCESS -> {
                    it.data?.let { statusData ->
                        cn0ContentALL.text = statusData.CN0
                        numSatsContentALL.text = statusData.satellitesCount
                    }
                }
                ERROR -> {
                }
            }
        }
    }

    override fun onSatelliteStatusChanged(status: android.location.GnssStatus) {
        val filteredGnssStatus = filterGnssStatus(status, selectedConstellation)

        viewModel?.obtainStatusParameters(filteredGnssStatus, selectedConstellation)
        skyplot.setGnssStatus(filteredGnssStatus)
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
        skyplot.onOrientationChanged(orientation, tilt)
    }

    override fun onGnssStopped() {
        skyplot.setStopped()
    }

    override fun onGnssStarted() {
        skyplot.setStarted()
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        //no-op
    }


}
