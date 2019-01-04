package com.inari.team.ui.statistics

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {

    companion object {
        const val GRAPH_TYPE: String = "graph_type"
        const val RMS: String = "RMS"
        const val CNO: String = "CNO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val type = intent?.getStringExtra(GRAPH_TYPE)

        when (type) {
            RMS -> {
                //crides al nom de la funcio de RMS
            }
            CNO -> {
            }
        }

    }
}
