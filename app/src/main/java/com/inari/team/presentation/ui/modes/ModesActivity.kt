package com.inari.team.presentation.ui.modes

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.inari.team.R
import com.inari.team.presentation.model.Mode
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.toast
import kotlinx.android.synthetic.main.activity_modes.*
import kotlinx.android.synthetic.main.dialog_new_mode.view.*

class ModesActivity : AppCompatActivity() {

    private val mAdapter = ModesListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modes)

        setToolbar()

        modesRVList.layoutManager = LinearLayoutManager(this)

        modesRVList.adapter = mAdapter

        fabNewMode.setOnClickListener {
            showNewModeDialog()
        }

    }

    private fun setToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Modes"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun showNewModeDialog() {

        val dialog = AlertDialog.Builder(this).create()
        val layout = View.inflate(this, R.layout.dialog_new_mode, null)
        dialog.window?.let { wind ->
            wind.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setView(layout)
        layout.createButton.setOnClickListener {
            createMode(layout, dialog)
        }
        dialog.show()

    }

    private fun createMode(layout: View?, dialog: AlertDialog) {
        var name = ""
        val constellations = arrayListOf<Int>()
        val bands = arrayListOf<Int>()
        val corrections = arrayListOf<Int>()
        var algorithm = 0

        layout?.let {
            name = it.modeNameTextEdit.text.toString() // set the name
            if (it.constOption1.isChecked) constellations.add(Mode.CONST_GPS) // set selected constellations
            if (it.constOption2.isChecked) constellations.add(Mode.CONST_GAL)
            if (it.bandsOption1.isChecked) bands.add(Mode.BAND_L1) // set selected bands
            if (it.bandsOption2.isChecked) bands.add(Mode.BAND_L5)
            if (it.correctionsOption1.isChecked) corrections.add(Mode.CORR_IONOSPHERE)  // set selected corrections
            if (it.correctionsOption2.isChecked) corrections.add(Mode.CORR_TROPOSPHERE)
            if (it.correctionsOption3.isChecked) corrections.add(Mode.CORR_MULTIPATH)
            if (it.correctionsOption4.isChecked) corrections.add(Mode.CORR_CAMERA)
            if (it.algorithm1.isChecked) algorithm = Mode.ALG_LS  // set selected algorithm
            if (it.algorithm2.isChecked) algorithm = Mode.ALG_WLS
            if (it.algorithm3.isChecked) algorithm = Mode.ALG_KALMAN
        }

        val modesList = AppSharedPreferences.getInstance().getModesList()
        if (modeCanBeAdded(name, constellations, bands, modesList)) {
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
            dialog.dismiss()
            mAdapter.update()
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
}
