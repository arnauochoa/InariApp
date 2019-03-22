package com.inari.team.presentation.ui.splash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.*
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.ui.main.MainActivity
import java.io.File
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    private val root: File =
        android.os.Environment.getExternalStorageDirectory()

    companion object {
        private const val TIME_OUT = 2000L
        private const val APP_ROOT: String = "/Inari/Logs/"
        const val PERMISSIONS_CODE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        enableFullScreen(window)

        Handler().postDelayed({

            if (AppSharedPreferences.getInstance().getModesList().isEmpty()) {
                addDefaultModes()
                try {
                    val dir =
                        File(root.absolutePath + APP_ROOT)
                    dir.mkdirs()
                } catch (e: IOException) {
                }
            }



            if (checkPermissionsList(
                    arrayOf(
                        PERMISSION_WRITE_EXTERNAL_STORAGE,
                        PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_ACCESS_FINE_LOCATION
                    )
                )
            ) {
                goToMainActivity()
            } else {
                requestPermissionss(
                    arrayOf(
                        PERMISSION_ACCESS_FINE_LOCATION,
                        PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE
                    ), PERMISSIONS_CODE
                )
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
            Mode.ALG_LS,
            avgTime = 5L,
            isSelected = true
        )
        val mode2 = Mode(
            1,
            "Galileo WLS",
            arrayListOf(Mode.CONST_GAL),
            arrayListOf(Mode.BAND_L1),
            arrayListOf(Mode.CORR_IONOSPHERE, Mode.CORR_TROPOSPHERE),
            Mode.ALG_WLS,
            avgTime = 5L,
            isSelected = false
        )
        val mode3 = Mode(
            2,
            "Multiconst Iono-Free",
            arrayListOf(Mode.CONST_GPS, Mode.CONST_GAL),
            arrayListOf(Mode.BAND_L1, Mode.BAND_L5),
            arrayListOf(Mode.CORR_TROPOSPHERE, Mode.CORR_IONOFREE),
            Mode.ALG_WLS,
            avgTime = 5L,
            isSelected = false
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
            if (checkPermissionsList(
                    arrayOf(
                        PERMISSION_WRITE_EXTERNAL_STORAGE,
                        PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_ACCESS_FINE_LOCATION
                    )
                )
            ) {
                goToMainActivity()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}
