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
import com.inari.team.computation.converters.ecef2lla
import com.inari.team.computation.converters.lla2ecef
import com.inari.team.computation.converters.toGeod
import com.inari.team.computation.converters.toTopocent
import com.inari.team.computation.data.LlaLocation
import com.inari.team.computation.utils.applyMod
import com.inari.team.computation.utils.checkGalState
import com.inari.team.computation.utils.checkTowDecode
import com.inari.team.computation.utils.checkTowKnown
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.addDefaultModes
import com.inari.team.core.utils.extensions.*
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
        clLogo.startAnimation(fadeIn)

        if (mPrefs.getModesList().isEmpty()) {
            mPrefs.saveModes(addDefaultModes())
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

        // TODO: remove this
        testMath()
    }

    // TODO: remove this
    private fun testMath() {
        //test lla2ecef
        val llaLocation = LlaLocation(42.2383729097, 19.3774822039, 100.0)
        val llaLocation2 = LlaLocation(-53.9828324342, -2.3774822039, 1200.0)
        val ecefLocation = lla2ecef(llaLocation)
        val ecefLocation2 = lla2ecef(llaLocation2)
        val llaLocationRec = ecef2lla(ecefLocation)
        val llaLocation2Rec = ecef2lla(ecefLocation2)

        //test mod
        val num = applyMod(5.2, 3)

        //test check state
        val t1 = checkTowDecode(0)
        val t2 = checkTowKnown(0)
        val t3 = checkGalState(0)

        val x = doubleArrayOf(50000.0, 30000.0, 60000.0)
        val tgd = toGeod(6378137.0, 298.257223563, x[0], x[1], x[2])
        val dx = doubleArrayOf(200.0, 110.0, 521.0)
        val topocent = toTopocent(x, dx)

        val a = 1
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
