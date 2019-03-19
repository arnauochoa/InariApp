package com.inari.team.presentation.ui.settings

import android.app.Activity
import android.os.Bundle
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.presentation.model.GnssSettings
import kotlinx.android.synthetic.main.activity_gnss_settings.*
import javax.inject.Inject

class GnssSettingsActivity : BaseActivity() {

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gnss_settings)

        activityComponent.inject(this)

        setViews()
    }

    private fun setViews() {
        setSupportActionBar(gnssSettingsToolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.gnss_settings_title)

        resetGnssSettings.setOnClickListener {
            resetGnssSettings()
        }
    }

    private fun resetGnssSettings() {
        val filters = GnssSettings()
        mPrefs //save new settings
    }

    fun applyFilters() {
        //save filters to preferences

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
