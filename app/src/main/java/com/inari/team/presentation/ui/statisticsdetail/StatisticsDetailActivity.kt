package com.inari.team.presentation.ui.statisticsdetail

import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.presentation.model.Mode
import kotlinx.android.synthetic.main.activity_statistics.*
import javax.inject.Inject


class StatisticsDetailActivity : BaseActivity() {

    companion object {
        const val GRAPH_TYPE: String = "graph_type"
        const val RMS: String = "RMS"
        const val CNO: String = "CNO"
        const val MAP: String = "MAP"
        const val GRAPH4: String = "GRAPH4"
        const val GRAPH5: String = "GRAPH5"
        const val GRAPH6: String = "GRAPH6"
    }

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    private var hasCompared = false

    private var modes: ArrayList<Mode> = arrayListOf()
    private var modesNames: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        activityComponent.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)

        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        setToolbarTitle(type)

        buttonCompareSave.setOnClickListener {
            updateSaveButton()
        }

        setChartData()

    }

    private fun setChartData() {
        val entries = arrayListOf<Entry>()

        repeat(10) {
            entries.add(Entry(1.0f * it, 1.0f * it))
        }

        val dataSet = LineDataSet(entries, "Label")
        dataSet.color = ContextCompat.getColor(this, R.color.colorAccentLight)
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.colorLegend1)

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setToolbarTitle(type: String?) {
        when (type) {
            RMS -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_1))
                //crides al nom de la funcio de RMS
            }
            CNO -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_2))
                //crides al nom de la funcio de CN0
            }
            MAP -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_3))
            }
            GRAPH4 -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_4))
                //crides al nom de la funcio de GRAPH4
            }
            GRAPH5 -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_5))
                //crides al nom de la funcio de GRAPH5
            }
            GRAPH6 -> {
                supportActionBar?.setTitle(getString(R.string.stats_card_6))
                //crides al nom de la funcio de GRAPH6
            }
        }
    }

    private fun updateSaveButton() {
        if (!hasCompared) {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccentLight))
            buttonCompareSave.text = getString(R.string.compare_button)
            hasCompared = true
        } else {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
            buttonCompareSave.text = getString(R.string.save_button)
            hasCompared = false
        }
    }

}

