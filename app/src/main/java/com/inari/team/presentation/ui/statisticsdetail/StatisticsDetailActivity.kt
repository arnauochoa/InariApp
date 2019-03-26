package com.inari.team.presentation.ui.statisticsdetail

import android.location.GnssMeasurement
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.RelativeLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.context
import com.inari.team.core.utils.filterGnssStatus
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.status.StatusFragment
import kotlinx.android.synthetic.main.activity_statistics.*
import javax.inject.Inject


class StatisticsDetailActivity : BaseActivity(), GnssEventsListener {

    companion object {
        const val GRAPH_TYPE: String = "graph_type"
        const val ELEVATION_CNO: String = "Elevation_CNo"
        const val CNO_AGC: String = "CNO_AGC"
        const val MAP: String = "MAP"
        const val GRAPH4: String = "GRAPH4"
        const val GRAPH5: String = "GRAPH5"
        const val GRAPH6: String = "GRAPH6"

        const val BAND1_DOWN_THRES = 1575000000
        const val BAND1_UP_THRES = 1576000000
        const val BAND5_DOWN_THRES = 1176000000
        const val BAND5_UP_THRES = 1177000000

        class SatElevCNo(var elevation: Float, var cNo: Float)
    }

    private enum class Band {
        L1_E1, L5_E5
    }

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    private var hasStopped = false

    private var modes: ArrayList<Mode> = arrayListOf()
    private var modesNames: ArrayList<String> = arrayListOf()

    // Charts data
    private var satElevCNoList = arrayListOf<SatElevCNo>()
    private var agcCNoValues = arrayListOf<Pair<Double, Double>>()


    var type: String? = null

    var lineChart: LineChart? = null
    var scatterChart: ScatterChart? = null

    private var selectedBand = Band.L1_E1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)
        activityComponent.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        type = intent?.getStringExtra(GRAPH_TYPE)

        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        setView()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setView() {
        val rl = findViewById<RelativeLayout>(com.inari.team.R.id.chartLayout)

        buttonStopSave.setOnClickListener {
            updateSaveButton()
        }

        buttonClear.setOnClickListener {
            satElevCNoList = arrayListOf()
            agcCNoValues = arrayListOf()
            lineChart?.invalidate()
            scatterChart?.invalidate()
        }

        when (type) {
            ELEVATION_CNO -> {
                supportActionBar?.title = getString(com.inari.team.R.string.stats_card_1)

                // programmatically create a LineChart
                lineChart = LineChart(context)

                rl.addView(lineChart)

                setChartData()
            }
            CNO_AGC -> {
                supportActionBar?.title = getString(com.inari.team.R.string.stats_card_2)

                scatterChart = ScatterChart(context)
                scatterChart?.let {
                    it.xAxis.axisMaximum = 60f
                    it.xAxis.axisMinimum = -20f

                    it.axisLeft.axisMaximum = 60f
                    it.axisLeft.axisMinimum = 0f
                    it.axisRight.isEnabled = false


                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    it.layoutParams = lp
                    rl.addView(it)
                }
                setChartData()

            }
            MAP -> {
                supportActionBar?.setTitle(getString(com.inari.team.R.string.stats_card_3))
            }
            GRAPH4 -> {
                supportActionBar?.setTitle(getString(com.inari.team.R.string.stats_card_4))
                //crides al nom de la funcio de GRAPH4
            }
            GRAPH5 -> {
                supportActionBar?.setTitle(getString(com.inari.team.R.string.stats_card_5))
                //crides al nom de la funcio de GRAPH5
            }
            GRAPH6 -> {
                supportActionBar?.setTitle(getString(com.inari.team.R.string.stats_card_6))
                //crides al nom de la funcio de GRAPH6
            }
        }
    }

    private fun setChartData() {
        when (type) {
            ELEVATION_CNO -> {
                plotElevCNoGraph()
            }
            CNO_AGC -> {
                plotAgcCNoGraph(null)
            }
            MAP -> {

            }
            GRAPH4 -> {

            }
            GRAPH5 -> {

            }
            GRAPH6 -> {

            }
        }
    }

    private fun updateSaveButton() {
        if (!hasStopped) {
            buttonStopSave.setBackgroundColor(ContextCompat.getColor(this, com.inari.team.R.color.colorPrimaryLight))
            buttonStopSave.text = getString(com.inari.team.R.string.save_button)
            hasStopped = false

        } else {
            buttonStopSave.setBackgroundColor(ContextCompat.getColor(this, com.inari.team.R.color.stopButton))
            buttonStopSave.text = getString(com.inari.team.R.string.stop_button)
            hasStopped = true
        }
    }

    private fun plotElevCNoGraph() {
        val cnos = arrayListOf<Entry>()
        val elevations = arrayListOf<Entry>()
        var x = 1

        // Set chart points
        satElevCNoList.forEach {
            cnos.add(Entry(1.0f * x, it.cNo))
            elevations.add(Entry(1.0f * x, it.elevation))
            x++
        }

        // CNo line
        val cNosSet = LineDataSet(cnos, "CNo evolution (dB-Hz)")
        cNosSet.axisDependency = YAxis.AxisDependency.LEFT
        cNosSet.color = ContextCompat.getColor(this, com.inari.team.R.color.colorAccentLight)
        cNosSet.valueTextColor = ContextCompat.getColor(this, com.inari.team.R.color.colorLegend1)

        // Elevation line
        val elevationsSet = LineDataSet(elevations, "Elevation evolution (º)")
        elevationsSet.axisDependency = YAxis.AxisDependency.RIGHT
        elevationsSet.color = ContextCompat.getColor(this, com.inari.team.R.color.colorPrimaryLight)
        elevationsSet.valueTextColor = ContextCompat.getColor(this, com.inari.team.R.color.colorLegend1)

        // Join lines
        val dataSets = arrayListOf<ILineDataSet>(cNosSet, elevationsSet)

        // Refresh chart
        lineChart?.let {
            if (!hasStopped) {
                it.data = LineData(dataSets)
                it.invalidate()
            }
        }

    }

    private fun plotAgcCNoGraph(measurements: Collection<GnssMeasurement>?) {
        measurements?.let {
            it.forEach { meas ->
                if (meas.hasAutomaticGainControlLevelDb()) {
                    if (meas.hasCarrierFrequencyHz() && isSelectedBand(meas.carrierFrequencyHz))
                        agcCNoValues.add(Pair(meas.cn0DbHz, meas.automaticGainControlLevelDb))
                }
            }
        }

        val points = arrayListOf<Entry>()
        agcCNoValues.forEach { point ->
            points.add(Entry(point.first.toFloat(), point.second.toFloat())) // x: CNo, y: AGC
        }

        val pointsSet = ScatterDataSet(points, "")
        pointsSet.color = ContextCompat.getColor(this, com.inari.team.R.color.colorAccent)

        scatterChart?.let {
            if (!hasStopped) {
                it.data = ScatterData(pointsSet)
                it.invalidate()
            }
        }
    }

    private fun isSelectedBand(carrierFrequencyHz: Float): Boolean {
        return if (selectedBand == Band.L1_E1 &&
            carrierFrequencyHz > BAND1_DOWN_THRES &&
            carrierFrequencyHz < BAND1_UP_THRES
        ) {
            true
        } else if (selectedBand == Band.L5_E5 &&
            carrierFrequencyHz > BAND5_DOWN_THRES &&
            carrierFrequencyHz < BAND5_UP_THRES
        ) {
            true
        } else {
            false
        }
    }

    // Callbacks
    override fun onGnssStarted() {
    }

    override fun onGnssStopped() {
    }

    override fun onSatelliteStatusChanged(status: GnssStatus) {
        val filteredGnssStatus = filterGnssStatus(status, StatusFragment.Companion.CONSTELLATION.GPS)
        val selectedSat = 1
        with(filteredGnssStatus) {
            val satElevCNo =
                StatisticsDetailActivity.Companion.SatElevCNo(getElevationDegrees(selectedSat), getCn0DbHz(selectedSat))
            if (satElevCNoList.size == 100) {
                satElevCNoList.removeAt(0)
            }
            satElevCNoList.add(satElevCNo)
        }

        if (type.equals(ELEVATION_CNO)) {
            plotElevCNoGraph()
        }

    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        event?.let {
            if (type.equals(CNO_AGC)) {
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

}

