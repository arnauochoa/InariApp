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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.*
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.ui.main.MainActivity
import com.inari.team.presentation.ui.statisticsdetail.StatisticsDetailActivity.Companion.CNO_AGC
import com.inari.team.presentation.ui.statisticsdetail.StatisticsDetailActivity.Companion.ELEVATION_CNO
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

        const val MIN_ELEV = 0f
        const val MAX_ELEV = 90f
        const val MAX_CNO = 60f
        const val MIN_CNO = -40f
        const val MAX_AGC = 60f
        const val MIN_AGC = 10f
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

    var scatterChart: ScatterChart? = null

    private var selectedBand = L1_E1

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
        MainActivity.getInstance()?.subscribeToGnssEvents(this)
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
            scatterChart?.invalidate()
        }

        when (type) {
            ELEVATION_CNO -> {
                supportActionBar?.title = getString(com.inari.team.R.string.stats_card_1)

                // programmatically create a ScatterChart
                scatterChart = createScatterChart(context, MIN_ELEV, MAX_ELEV, MIN_CNO, MAX_CNO)
                scatterChart?.let {
                    rl.addView(scatterChart)
                }
                setChartData()
            }
            CNO_AGC -> {
                supportActionBar?.title = getString(com.inari.team.R.string.stats_card_2)

                // programmatically create a ScatterChart
                scatterChart = createScatterChart(context, MIN_CNO, MAX_CNO, MIN_AGC, MAX_AGC)
                scatterChart?.let {
                    rl.addView(scatterChart)
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
                // todo: no data message
                //plotElevCNoGraph()
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

    private fun plotElevCNoGraph(status: GnssStatus) {
        scatterChart?.let { chart ->
            if (!hasStopped) {
                val gpsGnssStatus = filterGnssStatus(status, StatusFragment.Companion.CONSTELLATION.GPS)
                val galGnssStatus = filterGnssStatus(status, StatusFragment.Companion.CONSTELLATION.GALILEO)

                val gpsValues = obtainCnoElevValues(selectedBand, gpsGnssStatus)
                val galValues = obtainCnoElevValues(selectedBand, galGnssStatus)

                val gpsPoints = arrayListOf<Entry>()
                val galPoints = arrayListOf<Entry>()
                gpsValues.forEach {
                    gpsPoints.add(Entry(it.elevation, it.cNo))
                }
                galValues.forEach {
                    galPoints.add(Entry(it.elevation, it.cNo))
                }

                gpsPoints.sortBy { point -> point.x }
                galPoints.sortBy { point -> point.x }

                val gpsPointsSet = ScatterDataSet(gpsPoints, "GPS")
                val galPointsSet = ScatterDataSet(galPoints, "Galileo")

                gpsPointsSet.color = ContextCompat.getColor(this, com.inari.team.R.color.gpsColor)
                galPointsSet.color = ContextCompat.getColor(this, com.inari.team.R.color.galColor)

                val dataSets = arrayListOf<IScatterDataSet>(gpsPointsSet, galPointsSet)
                val scatterData = ScatterData(dataSets)
                scatterData.setDrawValues(false)
                chart.data = scatterData
                chart.invalidate()
            } // If has stopped, do nothing
        }
    }

    private fun plotAgcCNoGraph(measurements: Collection<GnssMeasurement>?) {
        scatterChart?.let { chart ->
            if (!hasStopped) {
                measurements?.let {
                    it.forEach { meas ->
                        if (meas.hasAutomaticGainControlLevelDb()) {
                            if (meas.hasCarrierFrequencyHz() && isSelectedBand(selectedBand, meas.carrierFrequencyHz))
                                agcCNoValues.add(Pair(meas.cn0DbHz, meas.automaticGainControlLevelDb))
                        }
                    }

                    val points = arrayListOf<Entry>()
                    agcCNoValues.forEach { point ->
                        points.add(Entry(point.first.toFloat(), point.second.toFloat())) // x: CNo, y: AGC
                    }

                    points.sortBy { point -> point.x }

                    val pointsSet = ScatterDataSet(points, "")
                    pointsSet.color = ContextCompat.getColor(this, com.inari.team.R.color.colorAccent)


                    val scatterData = ScatterData(pointsSet)
                    scatterData.setDrawValues(false)
                    chart.data = scatterData
                    chart.invalidate()
                }
            } // If has stopped, do nothing
        }
    }


    // Callbacks
    override fun onGnssStarted() {
    }

    override fun onGnssStopped() {
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        status?.let {
            if (type.equals(ELEVATION_CNO)) {
                plotElevCNoGraph(status)
            }
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

