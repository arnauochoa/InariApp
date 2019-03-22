package com.inari.team.presentation.ui.modes

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.getModeColor
import com.inari.team.core.utils.toast
import com.inari.team.presentation.model.Mode
import kotlinx.android.synthetic.main.activity_modes.*
import kotlinx.android.synthetic.main.dialog_new_mode.*
import kotlinx.android.synthetic.main.dialog_new_mode.view.*
import javax.inject.Inject

class ModesActivity : BaseActivity() {

    companion object {
        const val COMPARING_EXTRA: String = "comparing"
    }

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    private val mAdapter = ModesListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modes)
        activityComponent.inject(this)

        setToolbar()

        setViews()

    }

    private fun setViews() {
        modesRVList.layoutManager = LinearLayoutManager(this)

        modesRVList.adapter = mAdapter

        fabNewMode.setOnClickListener {
            //            navigator.navigateToGnssSettingsActivity()
            showNewModeDialog()
        }

        apply_gnss_modes.setOnClickListener {
            val selectedModes = mAdapter.getSelectedItems()

            selectedModes.forEachIndexed { index, mode ->
                mode.id = index
                mode.color = getModeColor(index)
            }

            mPrefs.saveModes(selectedModes)
            finish()
        }

        switchAvg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clAvgValue.visibility = VISIBLE
            } else {
                clAvgValue.visibility = GONE
            }
        }

        tvModesTitle.setOnClickListener {
            if (modesRVList.visibility == VISIBLE){
                modesRVList.visibility = GONE
            } else {
                modesRVList.visibility = VISIBLE
            }
            ivModesTitle.rotation = ivModesTitle.rotation + 180f
        }

        seekBarTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                tvAvgValue.text = "$progress s"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun setToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.modes)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun showNewModeDialog() {

        val dialog = AlertDialog.Builder(this).create()
        val layout = View.inflate(this, R.layout.dialog_new_mode, null)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(layout)
        layout.createButton.setOnClickListener {
            createMode(layout, dialog)
        }

        layout?.let {
            // Ionosphere and iono-free can't be selected at the same time
            it.correctionsOption3.isEnabled = !it.correctionsOption1.isChecked
            it.correctionsOption1.isEnabled = !it.correctionsOption3.isChecked

            it.correctionsOption1.setOnClickListener { l ->
                it.correctionsOption3.isEnabled = !l.correctionsOption1.isChecked
            }
            it.correctionsOption3.setOnClickListener { l ->
                it.correctionsOption1.isEnabled = !l.correctionsOption3.isChecked
            }
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
            if (it.correctionsOption3.isChecked) corrections.add(Mode.CORR_IONOFREE)
            if (it.algorithm1.isChecked) algorithm = Mode.ALG_LS  // set selected algorithm
            if (it.algorithm2.isChecked) algorithm = Mode.ALG_WLS
        }

        val modesList = AppSharedPreferences.getInstance().getModesList()
        if (modeCanBeAdded(name, constellations, bands, modesList)) {
            val mode = Mode(
                modesList.size,
                name,
                constellations,
                bands,
                corrections,
                algorithm,
                avgTime = 5L,
                isSelected = false
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
