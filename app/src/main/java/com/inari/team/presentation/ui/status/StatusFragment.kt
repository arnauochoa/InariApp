package com.inari.team.presentation.ui.status


import android.graphics.Color
import android.location.GnssMeasurementsEvent
import android.location.Location
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.DataState.*
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.filterGnssStatus
import com.inari.team.core.utils.getCNo
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.takeTwoDecimalsToDouble
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.StatusData
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.view_cno_indicator.*
import timber.log.Timber
import javax.inject.Inject

class StatusFragment : BaseFragment(), GnssEventsListener {

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    @Inject
    lateinit var navigator: Navigator

    companion object {

        enum class CONSTELLATION(var id: Int) {
            ALL(-1), GALILEO(GnssStatus.CONSTELLATION_GALILEO), GPS(GnssStatus.CONSTELLATION_GPS)
        }
    }

    private var viewModel: StatusViewModel? = null

    private var selectedConstellation: CONSTELLATION = Companion.CONSTELLATION.ALL

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
        setViews()
    }

    override fun onResume() {
        super.onResume()
        tabLayout?.getTabAt(0)?.select()
        skyplot.setHorizonSelected(mPrefs.getSelectedMask().toFloat())
    }

    private fun setViews() {

        tvLegend?.setOnClickListener {
            clLegend?.visibility = if (clLegend.visibility == VISIBLE) {
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

            }
        })

    }

    //helpers
    private fun createTab(title: String): TabLayout.Tab {
        val tab = tabLayout.newTab().setCustomView(R.layout.item_filter_tab)
        tab.customView?.findViewById<TextView>(R.id.tvFilterTitle)?.text = title
        return tab
    }

    private fun setCNo(status: GnssStatus) {

        val cno = takeTwoDecimalsToDouble(getCNo(status, selectedConstellation))

        if (cno in 10.00..45.00) {
            seekBar?.setProgress(cno.toInt(), true)
        } else {
            seekBar?.setProgress(11, true)
        }

        seekBar?.isEnabled = false

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                        cn0Content?.text = statusData.CN0
                        numSatsContent?.text = statusData.satellitesCount
                    }
                }
                ERROR -> {
                }
            }
        }
    }

    override fun onSatelliteStatusChanged(status: android.location.GnssStatus) {
        Timber.d("onGnssCallback - Status - STATUS")
        val filteredGnssStatus = filterGnssStatus(status, selectedConstellation)

        viewModel?.obtainStatusParameters(filteredGnssStatus, selectedConstellation)
        skyplot?.setGnssStatus(filteredGnssStatus)
        setCNo(status = filteredGnssStatus)
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
        skyplot?.onOrientationChanged(orientation, tilt)
    }

    override fun onGnssStopped() {
        skyplot?.setStopped()
    }

    override fun onGnssStarted() {
        skyplot?.setStarted()
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        //no-op
    }

    override fun onLocationReceived(location: Location?) {
        //no-op
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
    }
}
