package com.inari.team.presentation.ui.statistics


import android.content.Context
import android.graphics.Color
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.presentation.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_statistics.*
import javax.inject.Inject


class StatisticsFragment : BaseFragment(), GnssEventsListener {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        MainActivity.getInstance()?.subscribeToGnssEvents(this)
    }

    override fun onResume() {
        super.onResume()
        tabLayout?.getTabAt(0)?.select()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)
    }

    private fun setViews(view: View) {

        //init default graph
        setAgcCNOGraph()

        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        tabLayout.addTab(createTab(L1))
        tabLayout.addTab(createTab(L5))

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
                    filterByFrequence(it)
                }


            }
        })

        val graphs = arrayListOf(GRAPH_AGC_CNO, GRAPH_AGC_CNO2, GRAPH_ERROR)
        spGraphType.adapter = GraphSpinnerAdapter(view.context, graphs)
        spGraphType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val graph = graphs[position]

                when (graph) {
                    GRAPH_AGC_CNO -> {
                        setAgcCNOGraph()
                    }
                    GRAPH_AGC_CNO2 -> {
                        setSecondGraph()
                    }

                }
            }

        }
    }

    fun setAgcCNOGraph() {
        context?.let {
            rlGraph.setBackgroundColor(ContextCompat.getColor(it, R.color.colorLegend1))
        }
    }

    fun setSecondGraph() {
        context?.let {
            rlGraph.setBackgroundColor(ContextCompat.getColor(it, R.color.colorLegend5))
        }
    }

    fun filterByFrequence(freq: Int) {
        when (freq) {
            0 -> {
                //L1
            }
            1 -> {
                //L5
            }
        }
    }

    private fun createTab(title: String): TabLayout.Tab {
        val tab = tabLayout.newTab().setCustomView(R.layout.item_filter_tab)
        tab.customView?.findViewById<TextView>(R.id.tvFilterTitle)?.text = title
        return tab
    }

    override fun onGnssStarted() {
    }

    override fun onGnssStopped() {
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
    }

    override fun onLocationReceived(location: Location?) {
    }

    companion object {
        const val L1 = "L1/E1"
        const val L5 = "L5/E5a"

        const val GRAPH_AGC_CNO = "AGC/CN0"
        const val GRAPH_AGC_CNO2 = "2vfdsv"
        const val GRAPH_ERROR = "Error plot"
    }
}
