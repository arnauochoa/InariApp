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
    private var hasCompared = false

    private var modes: ArrayList<Mode> = arrayListOf()
    private var modesNames: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)

        setSupportActionBar(statisticsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)
        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        newModeButton.setOnClickListener {
            startActivity(Intent(this@StatisticsActivity, ModesActivity::class.java))
            //showNewModeDialog()
        }

        setToolbarTitle(type)

        setAdapters()

        buttonCompareSave.setOnClickListener {
            changeButton()
        }
    }

    private fun changeButton() {
        if (!hasCompared) {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.saveButton))
            buttonCompareSave.text = getString(R.string.compare_button)
            hasCompared = true
        } else {
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.compareButton))
            buttonCompareSave.text = getString(R.string.save_button)
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

    private fun setAdapters() {

        val adapterA = ModesAdapter(this, modes, 0, 1)
        val adapterB = ModesAdapter(this, modes, 1, 0)


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
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}

