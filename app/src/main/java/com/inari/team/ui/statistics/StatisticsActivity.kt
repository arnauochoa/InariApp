package com.inari.team.ui.statistics

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 9f9532e... modes spinner added
=======
import android.view.Display
>>>>>>> a9192e2... spinners exclusion done, selected option does not work properly
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.utils.AppSharedPreferences
import kotlinx.android.synthetic.main.activity_statistics.*

<<<<<<< HEAD
=======
>>>>>>> f69f812... add statistics activity
=======
>>>>>>> 9f9532e... modes spinner added

class StatisticsActivity : AppCompatActivity() {

    companion object {
        const val GRAPH_TYPE: String = "graph_type"
        const val RMS: String = "RMS"
        const val CNO: String = "CNO"
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 9f9532e... modes spinner added
        const val MAP: String = "MAP"
        const val GRAPH4: String = "GRAPH4"
        const val GRAPH5: String = "GRAPH5"
        const val GRAPH6: String = "GRAPH6"
<<<<<<< HEAD
    }

    private val mPrefs = AppSharedPreferences.getInstance()
    private var hasCompared = false

    var modes: ArrayList<Mode> = arrayListOf()
    var modesNames: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.inari.team.R.layout.activity_statistics)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.getStringExtra(GRAPH_TYPE)
        modes = mPrefs.getModesList()
        modesNames = mPrefs.getModesNames()

        setToolbarTitle(type)

        setAdapters()

        buttonCompareSave.setOnClickListener{
            changeButton()
        }
    }

    private fun changeButton(){
        if (!hasCompared){
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.saveButton))
            //buttonCompareSave.text = "save"
            hasCompared = true
        }else{
            buttonCompareSave.setBackgroundColor(ContextCompat.getColor(this, R.color.compareButton))
            //buttonCompareSave.text = "compare"
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
                adapterA.updateList(adapterA.selectedMode, adapterB.selectedMode)
                adapterB.updateList(adapterB.selectedMode, adapterA.selectedMode)
                hasCompared = false
                changeButton()
            }

        }

        spinnerModeB.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapterB.selectedMode = adapterB.getItems()[position]
                adapterB.updateList(adapterB.selectedMode, adapterA.selectedMode)
                adapterA.updateList(adapterA.selectedMode, adapterB.selectedMode)
                hasCompared = false
                changeButton()
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

=======
=======
>>>>>>> 9f9532e... modes spinner added
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
<<<<<<< HEAD
>>>>>>> f69f812... add statistics activity
=======

>>>>>>> 9f9532e... modes spinner added
}
