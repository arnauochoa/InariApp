package com.inari.team.ui.statistics

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.ui.modes.ModesActivity
import com.inari.team.ui.statistics.StatisticsActivity.Companion.CNO
import com.inari.team.ui.statistics.StatisticsActivity.Companion.GRAPH4
import com.inari.team.ui.statistics.StatisticsActivity.Companion.GRAPH5
import com.inari.team.ui.statistics.StatisticsActivity.Companion.GRAPH6
import com.inari.team.ui.statistics.StatisticsActivity.Companion.MAP
import com.inari.team.ui.statistics.StatisticsActivity.Companion.RMS
import com.inari.team.utils.AppSharedPreferences
import kotlinx.android.synthetic.main.activity_statistics.*


class StatisticsActivity : AppCompatActivity() {

    companion object {
        const val GRAPH_TYPE: String = "graph_type"
        const val RMS: String = "RMS"
        const val CNO: String = "CNO"
        const val MAP: String = "MAP"
        const val GRAPH4: String = "GRAPH4"
        const val GRAPH5: String = "GRAPH5"
        const val GRAPH6: String = "GRAPH6"
        private const val DEFAULT_MODE_1: Int = 0
        private const val DEFAULT_MODE_2: Int = 1
    }

    private val mPrefs = AppSharedPreferences.getInstance()
    private var hasCompared = false

    private var modes: ArrayList<Mode> = arrayListOf()
    private var modesNames: ArrayList<String> = arrayListOf()

    private lateinit var mode1: Mode
    private lateinit var mode2: Mode

    private var isMap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)

        setSupportActionBar(statisticsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)
        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        mode1 = modes[DEFAULT_MODE_1]
        mode2 = modes[DEFAULT_MODE_2]

        newModeButton.setOnClickListener {
            startActivity(Intent(this@StatisticsActivity, ModesActivity::class.java))
        }

        setToolbarTitle(type)

        setAdapters()
        setGraph()

        buttonCompareSave.setOnClickListener {
            changeButton()
        }
    }

    override fun onRestart() {
        super.onRestart()
        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()
        setAdapters()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun changeButton() {
        if (!hasCompared) {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccentLight))
            buttonCompareSave.text = getString(R.string.compare_button)
            hasCompared = true
        } else {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
            buttonCompareSave.text = getString(R.string.save_button)
            setGraph()
        }
    }

    private fun setGraph() {
        if (!isMap) {
            val graphNum = (mode1.id + mode2.id) % 8
            when (graphNum) {
                1 -> {
                    graph.setImageResource(R.drawable.graph1)
                }
                2 -> {
                    graph.setImageResource(R.drawable.graph2)
                }
                3 -> {
                    graph.setImageResource(R.drawable.graph3)
                }
                4 -> {
                    graph.setImageResource(R.drawable.graph4)
                }
                5 -> {
                    graph.setImageResource(R.drawable.graph5)
                }
                6 -> {
                    graph.setImageResource(R.drawable.graph6)
                }
                7 -> {
                    graph.setImageResource(R.drawable.graph7)
                }
                8 -> {
                    graph.setImageResource(R.drawable.graph8)
                }
            }
        } else {
            val graphNum = (mode1.id + mode2.id) % 4
            when (graphNum) {
                1 -> {
                    graph.setImageResource(R.drawable.map1)
                }
                2 -> {
                    graph.setImageResource(R.drawable.map2)
                }
                3 -> {
                    graph.setImageResource(R.drawable.map3)
                }
                4 -> {
                    graph.setImageResource(R.drawable.map4)
                }
            }
        }
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
                isMap = true
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

    private fun setAdapters() {

        val adapterA = ModesAdapter(this, modes, DEFAULT_MODE_1, DEFAULT_MODE_2)
        val adapterB = ModesAdapter(this, modes, DEFAULT_MODE_2, DEFAULT_MODE_1)


        spinnerModeA.adapter = adapterA
        spinnerModeB.adapter = adapterB

        spinnerModeA.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapterA.selectedMode = adapterA.getItems()[position]
                adapterA.selectedMode?.let { selectedA ->
                    adapterB.selectedMode?.let { selectedB ->
                        adapterA.updateList(selectedA, selectedB)
                        adapterB.updateList(selectedB, selectedA)
                    }
                }
                spinnerModeA.setSelection(0)
                hasCompared = false
                changeButton()
                mode1 = adapterA.selectedMode!!
            }

        }

        spinnerModeB.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapterB.selectedMode = adapterB.getItems()[position]
                adapterB.selectedMode?.let { selectedB ->
                    adapterA.selectedMode?.let { selectedA ->
                        adapterB.updateList(selectedB, selectedA)
                        adapterA.updateList(selectedA, selectedB)
                    }
                }
                spinnerModeB.setSelection(0)
                hasCompared = false
                changeButton()
                mode2 = adapterB.selectedMode!!
            }

        }
    }

}

