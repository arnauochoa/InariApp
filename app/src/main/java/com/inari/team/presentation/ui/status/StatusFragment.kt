package com.inari.team.presentation.ui.status


import android.graphics.Color
import android.location.GnssMeasurementsEvent
import android.location.Location
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.DataState.*
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.filterGnssStatus
import com.inari.team.core.utils.getCNo
import com.inari.team.core.utils.skyplot.DilutionOfPrecision
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.skyplot.GpsTestUtil
import com.inari.team.core.utils.takeTwoDecimalsToDouble
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.StatusData
import com.inari.team.presentation.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.view_cno_indicator.*

class StatusFragment : BaseFragment(), GnssEventsListener {

    companion object {
        const val GPS_DOP_TAG = "\$GPGSA"
        const val GAL_DOP_TAG = "\$GAGSA"

        enum class CONSTELLATION(var id: Int) {
            ALL(-1), GALILEO(GnssStatus.CONSTELLATION_GALILEO), GPS(GnssStatus.CONSTELLATION_GPS)
        }
    }

    private var viewModel: StatusViewModel? = null

    private var selectedConstellation: CONSTELLATION = Companion.CONSTELLATION.ALL

    private var dopGps: DilutionOfPrecision? = null
    private var dopGal: DilutionOfPrecision? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        viewModel = withViewModel(viewModelFactory) {
            observe(status, ::updateStatusData)
        }

        setHasOptionsMenu(false)

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
        MainActivity.getInstance()?.subscribeToGnssEvents(this)
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
                    ?.setBackgroundResource(R.drawable.bg_corners_blue)
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(R.drawable.bg_corners_gray)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(R.drawable.bg_corners_blue)
                tab?.position?.let {
                    if (it < CONSTELLATION.values().size) {
                        selectedConstellation = CONSTELLATION.values()[it]
                    }
                }

                legend_cv.visibility = if (selectedConstellation == Companion.CONSTELLATION.ALL) VISIBLE else GONE
                showDop()
            }
        })

    }

    //helpers
    private fun createTab(title: String): TabLayout.Tab {
        val tab = tabLayout.newTab().setCustomView(R.layout.item_filter_tab)
        tab.customView?.findViewById<TextView>(R.id.tvFilterTitle)?.text = title
        return tab
    }

    private fun showDop() {
        val dopToShow = when (selectedConstellation) {
            Companion.CONSTELLATION.GPS -> dopGps
            Companion.CONSTELLATION.GALILEO -> dopGal
            Companion.CONSTELLATION.ALL -> dopGps //TODO: what DOP should we show?
        }

        if (dopToShow == null) {
            pdopContent.text = "--"
            hvdopContent.text = "--/--"
        } else {
            pdopContent.text = "${dopToShow.positionDop}"
            hvdopContent.text = "${dopToShow.horizontalDop}/${dopToShow.verticalDop}"
        }
    }

    private fun setCNo(status: GnssStatus) {

        val cno = takeTwoDecimalsToDouble(getCNo(status, selectedConstellation))

        if (cno in 10.00..45.00) {
            seekBar.setProgress(cno.toInt(), true)
            clIndicator.visibility = VISIBLE
        } else {
            clIndicator.visibility = INVISIBLE
        }

        seekBar.isEnabled = false

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, p: Int, fromUser: Boolean) {
                seekBar?.let {
                    tvCnoAvg?.text = "$cno"
                    clIndicator?.x = it.thumb.bounds.exactCenterX()
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

    }

    //callbacks
    private fun updateStatusData(data: Data<StatusData>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                }
                SUCCESS -> {
                    it.data?.let { statusData ->
                        cn0Content.text = statusData.CN0
                        numSatsContent.text = statusData.satellitesCount
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
        setCNo(status = filteredGnssStatus)
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

    override fun onLocationReceived(location: Location?) {
        //no-op
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
        //todo: If GpsTestUtil is translated to kotlin, pass selected constellation instead of constellation tag
        val newDopGps = GpsTestUtil.getDop(message, GPS_DOP_TAG)
        val newDopGal = GpsTestUtil.getDop(message, GAL_DOP_TAG)

        dopGps = newDopGps ?: dopGps
        dopGal = newDopGal ?: dopGal

        showDop()
    }
}
