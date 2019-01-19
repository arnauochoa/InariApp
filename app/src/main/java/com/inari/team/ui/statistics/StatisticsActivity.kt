package com.inari.team.ui.statistics

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.utils.AppSharedPreferences
import com.inari.team.utils.toast
import kotlinx.android.synthetic.main.activity_statistics.*
import kotlinx.android.synthetic.main.dialog_new_mode.view.*


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

    var modes: ArrayList<Mode> = arrayListOf()
    var modesNames: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)

        setSupportActionBar(statisticsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)
        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        newModeButton.setOnClickListener {
            showNewModeDialog()
        }

        setToolbarTitle(type)

        setAdapters()

        buttonCompareSave.setOnClickListener {
            changeButton()
        }
    }

    private fun showNewModeDialog() {

        val dialog = AlertDialog.Builder(this).create()
        val layout = View.inflate(this, R.layout.dialog_new_mode, null)
        dialog.window?.let { wind ->
            wind.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setView(layout)
        layout.createButton.setOnClickListener {
            createMode(layout)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun createMode(layout: View?) {
        var name = ""
        val constellations = arrayListOf<Int>()
        val bands = arrayListOf<Int>()
        val corrections = arrayListOf<Int>()
        var algorithm = 0

        layout?.let {
            name = it.modeNameTextEdit.text.toString()
            // set selected constellations
            if (it.constOption1.isChecked) constellations.add(Mode.CONST_GPS)
            if (it.constOption2.isChecked) constellations.add(Mode.CONST_GAL)
            // set selected bands
            if (it.bandsOption1.isChecked) bands.add(Mode.BAND_L1)
            if (it.bandsOption2.isChecked) bands.add(Mode.BAND_L5)
            // set selected corrections
            if (it.correctionsOption1.isChecked) corrections.add(Mode.CORR_IONOSPHERE)
            if (it.correctionsOption2.isChecked) corrections.add(Mode.CORR_TROPOSPHERE)
            if (it.correctionsOption3.isChecked) corrections.add(Mode.CORR_MULTIPATH)
            if (it.correctionsOption4.isChecked) corrections.add(Mode.CORR_CAMERA)
            // set selected algorithm
            if (it.algorithm1.isChecked) algorithm = Mode.ALG_LS
            if (it.algorithm2.isChecked) algorithm = Mode.ALG_WLS
            if (it.algorithm3.isChecked) algorithm = Mode.ALG_KALMAN
        }

        val modesList = AppSharedPreferences.getInstance().getModesList()
        if ( modeCanBeAdded(name, constellations, bands, modesList)) {
            val mode = Mode(
                modesList.size,
                name,
                constellations,
                bands,
                corrections,
                algorithm
            )
            AppSharedPreferences.getInstance().saveMode(mode)
            toast("Mode created")
        }

    }

    private fun modeCanBeAdded(
        name: String,
        constellations: ArrayList<Int>,
        bands: ArrayList<Int>,
        modesList: ArrayList<Mode>
    ): Boolean {

        var canBeAdded = false

        if (name.isNotBlank()) {
            if (!modesList.any { mode -> mode.name == name }) { //if name is not repeated
                if (constellations.isNotEmpty()) { // if one constellation is selected
                    if (bands.isNotEmpty()) { //if one band is selected
                        canBeAdded = true
                    } else { //if no band is selected
                        toast("At least one band must be selected")
                    }
                } else { //if no constellation is selected
                    toast("At least one constellattion must be selected")
                }
            } else { //if name already exists
                toast("This name already exists")
            }
        } else {//if name is blank
            toast("Name can not be blank")
        }
        return canBeAdded
    }


    private fun nameIsNotRepeated(name: String, modesList: ArrayList<Mode>): Boolean {
        return !modesList.any { mode -> mode.name == name }
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

