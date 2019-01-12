package com.inari.team.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.utils.AppSharedPreferences

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TIME_OUT = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0)
            } else {

                if (AppSharedPreferences.getInstance().getModesList().isEmpty()){
                    addDefaultModes()
                }

                goToMainActivity()
            }
        }, TIME_OUT)
    }

    private fun addDefaultModes() {

        val mode = Mode(
            0,
            "GPS LS",
            arrayListOf(Mode.CONST_GPS),
            arrayListOf(Mode.BAND_L1),
            arrayListOf(Mode.CORR_IONOSPHERE, Mode.CORR_TROPOSPHERE),
            Mode.ALG_LS
        )
        val mode2 = Mode(
            1,
            "Galileo WLS",
            arrayListOf(Mode.CONST_GAL),
            arrayListOf(Mode.BAND_L1),
            arrayListOf(Mode.CORR_IONOSPHERE, Mode.CORR_TROPOSPHERE),
            Mode.ALG_WLS
        )
        val mode3 = Mode(
            2,
            "Multiconstellation Kalman",
            arrayListOf(Mode.CONST_GPS, Mode.CONST_GAL),
            arrayListOf(Mode.BAND_L1, Mode.BAND_L5),
            arrayListOf(Mode.CORR_IONOSPHERE, Mode.CORR_TROPOSPHERE, Mode.CORR_MULTIPATH),
            Mode.ALG_KALMAN
        )

        val list = arrayListOf<Mode>(mode, mode2, mode3)

        AppSharedPreferences.getInstance().saveModes(list)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                goToMainActivity()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}
