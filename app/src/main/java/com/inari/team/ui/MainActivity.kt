package com.inari.team.ui

import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.inari.team.R
import com.inari.team.ui.position.PositionFragment
import com.inari.team.ui.statistics.StatisticsFragment
import com.inari.team.ui.status.StatusFragment
import com.inari.team.utils.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationListener {

    companion object {
        private const val MIN_TIME = 1L
        private const val MIN_DISTANCE = 0.0F
    }

    private var locationManager: LocationManager? = null
    private var locationProvider: LocationProvider? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var gnssMeasurementsEventListener: GnssMeasurementsEvent.Callback? = null

    private var positionFragment = PositionFragment()
    private var statusFragment = StatusFragment()
    private var statisticsFragment = StatisticsFragment()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setViewPager()
        setBottomNavigation()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager?.getProvider(LocationManager.GPS_PROVIDER)

        startGnss()

    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(supportFragmentManager)

        pagerAdapter.addFragments(positionFragment, "Position")
        pagerAdapter.addFragments(statusFragment, "GNSS state")
        pagerAdapter.addFragments(statisticsFragment, "Statistics")

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 0
    }

    private fun setBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_position -> {
                    switchFragment(0)
                }
                R.id.action_status -> {
                    switchFragment(1)
                }
                R.id.action_statistics -> {
                    switchFragment(2)
                }
            }
            true
        }
    }

    private fun startGnss() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            gnssStatusListener = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    //once gnss status received, notice position fragments
                    positionFragment.onGnnsDataReceived(gnssStatus = status)
                    statusFragment.onGnssStatusReceived(gnssStatus = status)
                }
            }

            gnssMeasurementsEventListener = object : GnssMeasurementsEvent.Callback() {
                override fun onGnssMeasurementsReceived(measurementsEvent: GnssMeasurementsEvent?) {
                    positionFragment.onGnnsDataReceived(gnssMeasurementsEvent = measurementsEvent)
                }
            }

            locationManager?.requestLocationUpdates(locationProvider?.name, MIN_TIME, MIN_DISTANCE, this)
            locationManager?.registerGnssStatusCallback(gnssStatusListener)
            locationManager?.registerGnssMeasurementsCallback(gnssMeasurementsEventListener)
        } else {
            requestPermissionss(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), SplashActivity.PERMISSIONS_CODE

            )
        }

    }

    //helpers
    private fun switchFragment(id: Int) {
        viewPager.setCurrentItem(id, false)
    }

    //callbacks
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onLocationChanged(location: Location?) {
        positionFragment.onGnnsDataReceived(location = location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkPermissionsList(arrayOf(PERMISSION_ACCESS_FINE_LOCATION))) {
                startGnss()
            } else {
                toast("Location permissions are compulsory, please go to settings to enable.", Toast.LENGTH_LONG)
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }
}
