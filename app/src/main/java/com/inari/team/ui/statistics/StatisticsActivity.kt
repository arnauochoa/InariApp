package com.inari.team.ui.statistics

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.inari.team.R
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
    }

    private val mPrefs = AppSharedPreferences.getInstance()
    private val hasCompared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mPrefs.getModesNames())
        spinnerModeA.adapter = adapter
        spinnerModeB.adapter = adapter

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
                //crides al nom de la funcio de MAP
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
