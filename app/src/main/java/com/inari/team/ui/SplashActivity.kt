package com.inari.team.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.utils.*
import java.io.File
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    private val root: File =
        android.os.Environment.getExternalStorageDirectory()

    private val APP_ROOT: String = "/Inari/Logs/"

    companion object {
        private const val TIME_OUT = 2000L
        private const val PERMISSIONS_CODE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({

            if (AppSharedPreferences.getInstance().getModesList().isEmpty()) {
                addDefaultModes()
                try {
                    val dir =
                        File(root.absolutePath + APP_ROOT)
                    dir.mkdirs()
                }catch (e: IOException){
                }
            }



            if (checkPermissionsList(arrayOf(PERMISSION_WRITE_EXTERNAL_STORAGE,
                            PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_ACCESS_FINE_LOCATION))) {
                goToMainActivity()
            } else {
                requestPermissionss(arrayOf(PERMISSION_ACCESS_FINE_LOCATION,
                        PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE), PERMISSIONS_CODE)
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
                "Multiconst Kalman",
                arrayListOf(Mode.CONST_GPS, Mode.CONST_GAL),
                arrayListOf(Mode.BAND_L1, Mode.BAND_L5),
                arrayListOf(Mode.CORR_IONOSPHERE, Mode.CORR_TROPOSPHERE, Mode.CORR_MULTIPATH),
                Mode.ALG_KALMAN
        )

        val list = arrayListOf(mode, mode2, mode3)

        AppSharedPreferences.getInstance().saveModes(list)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkPermissionsList(arrayOf(PERMISSION_WRITE_EXTERNAL_STORAGE,
                            PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_ACCESS_FINE_LOCATION))) {
                goToMainActivity()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}
