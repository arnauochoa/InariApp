package com.inari.team.presentation.ui.statistics


import android.graphics.Color
import android.location.GnssMeasurement
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
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.inari.team.R
import com.inari.team.R.drawable
import com.inari.team.R.layout
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.createScatterChart
import com.inari.team.core.utils.filterGnssStatus
import com.inari.team.core.utils.isSelectedBand
import com.inari.team.core.utils.obtainCnoElevValues
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.presentation.ui.status.StatusFragment
import kotlinx.android.synthetic.main.fragment_statistics.*
import timber.log.Timber
import javax.inject.Inject


class StatisticsFragment : BaseFragment(), GnssEventsListener {

    @Inject
    lateinit var navigator: Navigator

    private var scatterChart: ScatterChart? = null

    private var agcCNoValues = arrayListOf<Pair<Double, Double>>()

    private var selectedBand = L1_E1

    private var hasStopped = false

    private var graph: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
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
        setAgcCNoGraph()

        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        tabLayout.addTab(createTab(L1_E1_text))
        tabLayout.addTab(createTab(L5_E5_text))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(drawable.bg_corners_blue)
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(drawable.bg_corners_gray)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<ConstraintLayout>(R.id.tab)
                    ?.setBackgroundResource(drawable.bg_corners_blue)
                tab?.position?.let { band ->
                    selectedBand = band
                    setGraph()
                }
            }
        })


        val graphs = arrayListOf(GRAPH_AGC_CNO, GRAPH_CNO_ELEV, GRAPH_ERROR)
        spGraphType.adapter = GraphSpinnerAdapter(view.context, graphs)
        spGraphType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                graph = graphs[position]

                setGraph()
            }
        }
    }

    fun setGraph() {
        rlGraph.removeAllViews()
        agcCNoValues = arrayListOf()
        when (graph) {
            GRAPH_AGC_CNO -> {
                setAgcCNoGraph()
            }
            GRAPH_CNO_ELEV -> {
                setCNoElevGraph()
            }
        }
    }

    // Different graphs builders
    private fun setAgcCNoGraph() {
        context?.let {
            // programmatically create a ScatterChart
            when (selectedBand) {
                L1_E1 -> scatterChart = createScatterChart(context, MIN_CNO, MAX_CNO, MIN_AGC_L1, MAX_AGC_L1)
                L5_E5 -> scatterChart = createScatterChart(context, MIN_CNO, MAX_CNO, MIN_AGC_L5, MAX_AGC_L5)
            }
            xAxisTitle.text = getString(R.string.CNoAxisTitle)
            yAxisTitle.text = getString(R.string.AGCAxisTitle)
            scatterChart?.let { chart ->
                chart.legend.isEnabled = true
                chart.description.isEnabled = false
                chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                rlGraph.addView(chart)
//                plotAgcCNoGraph(null)
            }
        }
    }

    private fun setCNoElevGraph() {
        context?.let {
            scatterChart = createScatterChart(context, MIN_ELEV, MAX_ELEV, MIN_CNO, MAX_CNO)
            xAxisTitle.text = getString(R.string.ElevAxisTitle)
            yAxisTitle.text = getString(R.string.CNoAxisTitle)
            scatterChart?.let { chart ->
                chart.legend.isEnabled = true
                chart.description.isEnabled = false
                chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                rlGraph.removeAllViews()
                rlGraph.addView(chart)
            }
        }
    }

    private fun createTab(title: String): TabLayout.Tab {
        val tab = tabLayout.newTab().setCustomView(layout.item_filter_tab)
        tab.customView?.findViewById<TextView>(R.id.tvFilterTitle)?.text = title
        return tab
    }

    // Specific plots
    private fun plotAgcCNoGraph(measurements: Collection<GnssMeasurement>?) {

        context?.let { c ->

            scatterChart?.let { chart ->

                measurements?.let {

                    // Obtain measurements for desired frequency band
                    it.forEach { meas ->
                        if (meas.hasAutomaticGainControlLevelDb()) {
                            if (meas.hasCarrierFrequencyHz() && isSelectedBand(selectedBand, meas.carrierFrequencyHz)) {
                                if (agcCNoValues.size == MAX_AGC_CNO_LENGTH) {
                                    agcCNoValues.removeAt(0)
                                }
                                agcCNoValues.add(Pair(meas.cn0DbHz, meas.automaticGainControlLevelDb))
                            }
                        }
                    }

                    // Gnenerate points with obtained and previous measurements
                    val points = arrayListOf<Entry>()
                    agcCNoValues.forEach { point ->
                        points.add(Entry(point.first.toFloat(), point.second.toFloat())) // x: CNo, y: AGC
                    }

                    // Separate on good and bad points for plotting on different colors
                    var goodAgcCNoPoints = arrayListOf<Entry>()
                    var badAgcCNoPoints = arrayListOf<Entry>()

                    when (selectedBand) {
                        L1_E1 -> {
                            goodAgcCNoPoints =
                                points.filter { point -> point.x > THRES_CN0_L1 && point.y > THRES_AGC_L1 } as ArrayList<Entry>
                            badAgcCNoPoints =
                                points.filter { point -> point.x < THRES_CN0_L1 || point.y < THRES_AGC_L1 } as ArrayList<Entry>
                        }
                        L5_E5 -> {
                            goodAgcCNoPoints =
                                points.filter { point -> point.x > THRES_CN0_L5 && point.y > THRES_AGC_L5 } as ArrayList<Entry>
                            badAgcCNoPoints =
                                points.filter { point -> point.x < THRES_CN0_L5 || point.y < THRES_AGC_L5 } as ArrayList<Entry>
                        }
                    }

                    // Sort points by CNo
                    goodAgcCNoPoints.sortBy { point -> point.x }
                    badAgcCNoPoints.sortBy { point -> point.x }

                    // Define style of set
                    val goodPointsSet = ScatterDataSet(goodAgcCNoPoints, "Nominal conditions")
                    goodPointsSet.color = ContextCompat.getColor(c, R.color.agcCnoGood)
                    val badPointsSet = ScatterDataSet(badAgcCNoPoints, "Possible jamming")
                    badPointsSet.color = ContextCompat.getColor(c, R.color.agcCnoBad)


                    // Join sets and plot them on the graph
                    val dataSets = arrayListOf<IScatterDataSet>(goodPointsSet, badPointsSet)
                    val scatterData = ScatterData(dataSets)
                    // Do not show labels on each point
                    scatterData.setDrawValues(false)
                    // Plot points on graph
                    chart.data = scatterData

                    // Update chart view
                    chart.invalidate()
                }
            } // If has stopped, do nothing
        }

    }

    private fun plotElevCNoGraph(status: GnssStatus) {
        context?.let { c ->
            scatterChart?.let { chart ->
                if (!hasStopped) {
                    // Obtain status separately for GPS and GAL
                    val gpsGnssStatus = filterGnssStatus(status, StatusFragment.Companion.CONSTELLATION.GPS)
                    val galGnssStatus = filterGnssStatus(status, StatusFragment.Companion.CONSTELLATION.GALILEO)

                    // Obtain desired values of Status: svid, elevation and CNo for given frequency band
                    val gpsValues = obtainCnoElevValues(selectedBand, gpsGnssStatus)
                    val galValues = obtainCnoElevValues(selectedBand, galGnssStatus)

                    // Generate points
                    val gpsPoints = arrayListOf<Entry>()
                    val galPoints = arrayListOf<Entry>()
                    gpsValues.forEach {
                        gpsPoints.add(Entry(it.elevation, it.cNo))
                    }
                    galValues.forEach {
                        galPoints.add(Entry(it.elevation, it.cNo))
                    }

                    // Sort points by elevation
                    gpsPoints.sortBy { point -> point.x }
                    galPoints.sortBy { point -> point.x }

                    // Define style of sets
                    val gpsPointsSet = ScatterDataSet(gpsPoints, "GPS")
                    val galPointsSet = ScatterDataSet(galPoints, "Galileo")


                    gpsPointsSet.color = ContextCompat.getColor(c, com.inari.team.R.color.gpsColor)
                    galPointsSet.color = ContextCompat.getColor(c, com.inari.team.R.color.galColor)

                    // Join sets and plot them on the graph
                    val dataSets = arrayListOf<IScatterDataSet>(gpsPointsSet, galPointsSet)
                    val scatterData = ScatterData(dataSets)
                    // Do not show labels on each point
                    scatterData.setDrawValues(false)
                    // Plot points on graph
                    chart.data = scatterData

                    // Update chart view
                    chart.invalidate()

                } // If user has stopped, do nothing
            }
        }
    }

    override fun onGnssStarted() {
    }

    override fun onGnssStopped() {
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        Timber.d("onGnssCallback - Status - STATISTICS")
        status?.let {
            if (graph == GRAPH_CNO_ELEV) {
                plotElevCNoGraph(status)
            }
        }
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        Timber.d("onGnssCallback - Measurement - STATISTICS")
        event?.let {
            if (graph == GRAPH_AGC_CNO) {
                plotAgcCNoGraph(it.measurements)
            }
        }
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
    }

    override fun onLocationReceived(location: Location?) {
    }

    companion object {
        const val L1_E1_text = "L1/E1"
        const val L5_E5_text = "L5/E5a"

        const val L1_E1 = 0
        const val L5_E5 = 1

        const val GRAPH_AGC_CNO = "AGC/CNo"
        const val GRAPH_CNO_ELEV = "CNo/Elevation"
        const val GRAPH_ERROR = "Error plot"

        const val MAX_AGC_CNO_LENGTH = 100

        const val MIN_ELEV = 0f
        const val MAX_ELEV = 90f
        const val MAX_CNO = 50f
        const val MIN_CNO = -20f
        const val MAX_AGC_L1 = 60f
        const val MIN_AGC_L1 = 10f
        const val MAX_AGC_L5 = 30f
        const val MIN_AGC_L5 = -10f

        const val THRES_AGC_L1 = 45f
        const val THRES_CN0_L1 = 5f
        const val THRES_AGC_L5 = 5f
        const val THRES_CN0_L5 = 0f
    }
}
