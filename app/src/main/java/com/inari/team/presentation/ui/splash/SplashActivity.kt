package com.inari.team.presentation.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.*
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.PositionParameters
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File
import java.io.IOException
import javax.inject.Inject


class SplashActivity : BaseActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    private val root: File =
        android.os.Environment.getExternalStorageDirectory()

    companion object {
        private const val TIME_OUT = 2000L
        private const val APP_ROOT: String = "/Inari/Logs/"
        const val PERMISSIONS_CODE = 99
    }

    private var isComingFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        activityComponent.inject(this)

        enableFullScreen(window)

        val fadeIn = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fadeIn.duration = 1500     // animation duration in milliseconds
        fadeIn.fillAfter = true
        tvTitle.startAnimation(fadeIn)
        ivLogo.startAnimation(fadeIn)

        if (mPrefs.getModesList().isEmpty()) {
            addDefaultModes()
            try {
                val dir =
                    File(root.absolutePath + APP_ROOT)
                dir.mkdirs()
            } catch (e: IOException) {
            }
        }

        Handler().postDelayed({

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

    override fun onResume() {
        super.onResume()
        if (isComingFromSettings) {
            Handler().postDelayed({

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
    }

    private fun addDefaultModes() {

        val mode = Mode(
            0,
            "GPS LS",
            arrayListOf(PositionParameters.CONST_GPS),
            arrayListOf(PositionParameters.BAND_L1),
            arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
            PositionParameters.ALG_LS,
            isSelected = false
        )
        val mode2 = Mode(
            1,
            "Galileo WLS",
            arrayListOf(PositionParameters.CONST_GAL),
            arrayListOf(PositionParameters.BAND_L1),
            arrayListOf(PositionParameters.CORR_IONOSPHERE, PositionParameters.CORR_TROPOSPHERE),
            PositionParameters.ALG_WLS,
            isSelected = false
        )
        val mode3 = Mode(
            2,
            "Multiconst Iono-Free",
            arrayListOf(PositionParameters.CONST_GPS, PositionParameters.CONST_GAL),
            arrayListOf(PositionParameters.BAND_L1, PositionParameters.BAND_L5),
            arrayListOf(PositionParameters.CORR_TROPOSPHERE, PositionParameters.CORR_IONOFREE),
            PositionParameters.ALG_WLS,
            isSelected = false
        )

        val list = arrayListOf(mode, mode2, mode3)

        mPrefs.saveModes(list)
    }

    private fun goToMainActivity() {
        navigator.navigateToMainActivity()
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
                val showRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                if (!showRationale) {
                    //"never ask again" box checked

                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Turn on the permissions to proceed")
                        .setMessage("In order to activate the permissions, go to settings...")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            goToSettings()
                        }
                        .create()

                    dialog.show()

                }
            }
        } else {

            val showRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            if (!showRationale) {
                //"never ask again" box checked

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Turn on the permissions to proceed")
                    .setMessage("In order to activate the permissions, go to settings...")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        goToSettings()
                    }
                    .create()

                dialog.show()

            } else {

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Permissions required")
                    .setMessage("In order to use the application, the location permissions must be granted")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionss(
                            arrayOf(
                                PERMISSION_ACCESS_FINE_LOCATION,
                                PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE
                            ), PERMISSIONS_CODE
                        )
                    }
                    .create()

                dialog.show()
            }
        }


    }

    private fun goToSettings() {
        val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(myAppSettings)
        isComingFromSettings = true
    }
}
