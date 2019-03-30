package com.inari.team.presentation.ui.statistics


import android.content.Context
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
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.inari.team.R
import com.inari.team.R.drawable
import com.inari.team.R.layout
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.*
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.main.MainListener
import com.inari.team.presentation.ui.status.StatusFragment
import kotlinx.android.synthetic.main.fragment_statistics.*
import timber.log.Timber
import javax.inject.Inject


class StatisticsFragment : BaseFragment(), GnssEventsListener {

    @Inject
    lateinit var navigator: Navigator

    private var mainListener: MainListener? = null

    private var scatterChart: ScatterChart? = null

    private var agcCNoValues = arrayListOf<Pair<Double, Double>>()

    private var computedPositions = arrayListOf<ResponsePvtMode>()

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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainListener = context as? MainListener
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
                tabLayout.visibility = View.VISIBLE
                setAgcCNoGraph()
            }
            GRAPH_CNO_ELEV -> {
                tabLayout.visibility = View.VISIBLE
                setCNoElevGraph()
            }
            GRAPH_ERROR -> {
                tabLayout.visibility = View.GONE
                setErrorGraph()
            }
        }

        scatterChart?.let { chart -> rlGraph.addView(chart) }
    }

    // Different graphs builders
    private fun setAgcCNoGraph() {
        context?.let { c ->
            // programmatically create a ScatterChart
            when (selectedBand) {
                L1_E1 -> scatterChart = createScatterChart(c, MIN_CNO_L1, MAX_CNO_L1, MIN_AGC_L1, MAX_AGC_L1)
                L5_E5 -> scatterChart = createScatterChart(c, MIN_CNO_L5, MAX_CNO_L5, MIN_AGC_L5, MAX_AGC_L5)
            }
            xAxisTitle.text = getString(R.string.cnoAxisTitle)
            yAxisTitle.text = getString(R.string.agcAxisTitle)
            scatterChart?.let { chart ->
                chart.legend.isEnabled = true
                chart.legend.isWordWrapEnabled = true
            }
            scatterChart?.axisLeft?.labelCount = 6
        }
    }

    private fun setCNoElevGraph() {
        context?.let { c ->
            scatterChart = createScatterChart(c, MIN_ELEV, MAX_ELEV, MIN_CNO_L1, MAX_CNO_L1)
            xAxisTitle.text = getString(R.string.elevAxisTitle)
            yAxisTitle.text = getString(R.string.cnoAxisTitle)
            scatterChart?.let { chart ->
                chart.legend.isEnabled = true
            }
            scatterChart?.xAxis?.labelCount = 10
        }
    }

    private fun setErrorGraph() {
        context?.let { c ->
            computedPositions = mainListener?.getComputedPositions() as ArrayList<ResponsePvtMode>
            scatterChart = createScatterChart(c, -EAST_LIM, EAST_LIM, -NORTH_LIM, NORTH_LIM)
            xAxisTitle.text = getString(R.string.eastAxisTitle)
            yAxisTitle.text = getString(R.string.northAxisTitle)
            scatterChart?.let { chart ->
                chart.legend.isEnabled = true
            }
            val limitLine = LimitLine(0.0f, "")
            limitLine.lineColor = ContextCompat.getColor(c, R.color.black)
            limitLine.lineWidth = 1f
            scatterChart?.xAxis?.addLimitLine(limitLine)
            scatterChart?.axisLeft?.addLimitLine(limitLine)
            scatterChart?.setDrawBorders(false)
            scatterChart?.xAxis?.setDrawAxisLine(false)
            scatterChart?.axisLeft?.setDrawAxisLine(false)
            plotErrorGraph()
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
                                if (agcCNoValues.size == MAX_AGC_CNO_POINTS) {
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

                    // Obtain threshold and points divided by threshold
                    var agcCNoThreshold = AgcCNoThreshold()
                    when (selectedBand) {
                        L1_E1 -> {
                            agcCNoThreshold = setAgcCNoThreshold(AGC_CNO_M, AGC_CNO_N_L1, points)
                        }
                        L5_E5 -> {
                            agcCNoThreshold = setAgcCNoThreshold(AGC_CNO_M, AGC_CNO_N_L5, points)
                        }
                    }

                    // Define style of set
                    val thresholdSet = ScatterDataSet(agcCNoThreshold.threshold, "RFI threshold")
                    thresholdSet.color = ContextCompat.getColor(c, R.color.agcCnoThreshold)
                    thresholdSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    thresholdSet.scatterShapeSize = 5.0f
                    val nominalPointsSet = ScatterDataSet(agcCNoThreshold.nominalPoints, "Nominal conditions")
                    nominalPointsSet.color = ContextCompat.getColor(c, R.color.agcCnoNominal)
                    nominalPointsSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    val interfPointsSet = ScatterDataSet(agcCNoThreshold.interferencePoints, "Possible interference")
                    interfPointsSet.color = ContextCompat.getColor(c, R.color.agcCnoInterf)
                    interfPointsSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)

                    // Join sets and plot them on the graph
                    val dataSets = arrayListOf<IScatterDataSet>(thresholdSet, nominalPointsSet, interfPointsSet)
                    val scatterData = ScatterData(dataSets)
                    // Do not show labels on each point
                    scatterData.setDrawValues(false)
                    scatterData.isHighlightEnabled = false
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

                    // Sort points by elevation to plot them
                    gpsPoints.sortBy { point -> point.x }
                    galPoints.sortBy { point -> point.x }

                    // Define style of sets
                    val gpsPointsSet = ScatterDataSet(gpsPoints, "GPS")
                    val galPointsSet = ScatterDataSet(galPoints, "Galileo")

                    gpsPointsSet.color = ContextCompat.getColor(c, com.inari.team.R.color.gpsColor)
                    gpsPointsSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    galPointsSet.color = ContextCompat.getColor(c, com.inari.team.R.color.galColor)
                    galPointsSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)

                    // Join sets and plot them on the graph
                    val dataSets = arrayListOf<IScatterDataSet>(gpsPointsSet, galPointsSet)
                    val scatterData = ScatterData(dataSets)
                    // Do not show labels on each point
                    scatterData.setDrawValues(false)
                    scatterData.isHighlightEnabled = false
                    // Plot points on graph
                    chart.data = scatterData
                    // Update chart view
                    chart.invalidate()

                } // If user has stopped, do nothing
            }
        }
    }

    private fun plotErrorGraph() {
        context?.let { c ->
            scatterChart?.let { chart ->
                // Separate by modes
                val positionsByMode = computedPositions.groupBy { pos -> pos.modeName }

                // Generate points by mode
                val dataSets = arrayListOf<IScatterDataSet>()
                positionsByMode.keys.forEach {
                    val modePositions = arrayListOf<Entry>()
                    val color = positionsByMode[it]?.get(0)?.modeColor ?: 0
                    // For every position, get point as error between computed and reference position
                    positionsByMode[it]?.forEach { pos ->
                        val error = computeErrorNE(pos.refPosition, pos.refAltitude, pos.compPosition)
                        modePositions.add(Entry(error[1].toFloat(), error[0].toFloat())) //x: East, y: North
                    }

                    modePositions.sortBy { point -> point.x }
                    val pointsSet = ScatterDataSet(modePositions, it)
                    try {
                        pointsSet.color = ContextCompat.getColor(c, getLegendColor(color))
                    } catch (e: Exception) {
                    }
                    pointsSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    dataSets.add(pointsSet)
                }
                val scatterData = ScatterData(dataSets)
                // Do not show labels on each point
                scatterData.setDrawValues(false)
                scatterData.isHighlightEnabled = false
                // Plot points on graph
                chart.data = scatterData

                chart.invalidate()

            }
        }
    }

    // Callbacks
    fun onPositionsCalculated(positions: List<ResponsePvtMode>) {
        if (graph == GRAPH_ERROR) {
            positions.forEach { pos ->
                if (computedPositions.size == MAX_POS_POINTS) {
                    computedPositions.removeAt(0)
                }
                computedPositions.add(pos)
            }
            plotErrorGraph()
        }
    }

    fun onStopComputing() {
        computedPositions.clear()
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

        //Graph names
        const val GRAPH_AGC_CNO = "AGC/CNo"
        const val GRAPH_CNO_ELEV = "CNo/Elevation"
        const val GRAPH_ERROR = "Error plot"

        // Maximum number of points
        const val MAX_AGC_CNO_POINTS = 200
        const val MAX_POS_POINTS = 500

        // Limit values for graphs
        const val MIN_ELEV = 0f // º
        const val MAX_ELEV = 90f // º
        const val MAX_CNO_L1 = 45f // dB
        const val MIN_CNO_L1 = 0f // dB
        const val MAX_CNO_L5 = 30f // dB
        const val MIN_CNO_L5 = -5f // dB
        const val MAX_AGC_L1 = 45f // dB-Hz
        const val MIN_AGC_L1 = 30f // dB-Hz
        const val MAX_AGC_L5 = 15f // dB-Hz
        const val MIN_AGC_L5 = -5f // dB-Hz
        const val NORTH_LIM = 200f // m
        const val EAST_LIM = 200f // m

        // AGC-CNO threshold values: y=mx+n
        const val AGC_CNO_M = -0.1f
        const val AGC_CNO_N_L1 = 45f
        const val AGC_CNO_N_L5 = 9f
    }
}
