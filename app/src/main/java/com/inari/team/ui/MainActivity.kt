package com.inari.team.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.inari.team.R
import com.inari.team.ui.position.PositionFragment
import com.inari.team.ui.statistics.StatisticsFragment
import com.inari.team.ui.status.all_status.AllStatusFragment
import com.inari.team.ui.status.gps_status.GPSStatusFragment
import com.inari.team.ui.status.galileo_status.GalileoStatusFragment
import com.inari.team.ui.status.StatusFragment
import com.inari.team.utils.BarAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationListener, PositionFragment.PositionListener,
    StatusFragment.StatusListener {

    companion object {
        private const val MIN_TIME = 1L
        private const val MIN_DISTANCE = 0.0F
    }

    private var locationManager: LocationManager? = null
    private var locationProvider: LocationProvider? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var gnssMeasurementsEventListener: GnssMeasurementsEvent.Callback? = null
    private var gnssNavigationMessageListener: GnssNavigationMessage.Callback? = null

    private var positionFragment: PositionFragment? = null
    private var gpsStatusFragment: GPSStatusFragment? = null
    private var galileoStatusFragment: GalileoStatusFragment? = null
    private var allStatusFragment: AllStatusFragment? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationProvider = locationManager?.getProvider(LocationManager.GPS_PROVIDER)

        startGnss()

        setViewPager()
        setBottomNavigation()
        setGnssCallbacks()
    }

    @SuppressLint("MissingPermission")
    private fun startGnss() {
        locationManager?.requestLocationUpdates(locationProvider?.name, MIN_TIME, MIN_DISTANCE, this)
    }

    //setters
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun setGnssCallbacks() {
        gnssStatusListener = object : GnssStatus.Callback() {
            override fun onStarted() {
                Log.d("Gnss Callbacks", "GnssStatus.Callback onStarted()")
            }

            override fun onStopped() {
                Log.d("Gnss Callbacks", "GnssStatus.Callback onStopped()")
            }

            override fun onFirstFix(ttffMillis: Int) {
                Log.d("Gnss Callbacks", "GnssStatus.Callback onFirstFix()")
            }

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                //once gnss status received, notice position fragment
                positionFragment?.onGnnsDataReceived(gnssStatus = status)
                gpsStatusFragment?.onGnssStatusReceived(status)
                galileoStatusFragment?.onGnssStatusReceived(status)
                allStatusFragment?.onGnssStatusReceived(status)

            }
        }
        locationManager?.registerGnssStatusCallback(gnssStatusListener)

        gnssMeasurementsEventListener = object : GnssMeasurementsEvent.Callback() {
            override fun onGnssMeasurementsReceived(measurementsEvent: GnssMeasurementsEvent?) {
                positionFragment?.onGnnsDataReceived(gnssMeasurementsEvent = measurementsEvent)
            }
        }
        locationManager?.registerGnssMeasurementsCallback(gnssMeasurementsEventListener)

        gnssNavigationMessageListener = object : GnssNavigationMessage.Callback() {
            override fun onGnssNavigationMessageReceived(navigationMessage: GnssNavigationMessage?) {
                positionFragment?.onGnnsDataReceived(gnssNavigationMessage = navigationMessage)
            }
        }
        locationManager?.registerGnssNavigationMessageCallback(gnssNavigationMessageListener)

    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(supportFragmentManager)

        positionFragment = PositionFragment()

        pagerAdapter.addFragments(positionFragment, "Position")
        pagerAdapter.addFragments(StatusFragment(), "GNSS state")
        pagerAdapter.addFragments(StatisticsFragment(), "Statistics")

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

    //helpers
    private fun switchFragment(id: Int) {
        viewPager.setCurrentItem(id, false)
    }

    //fragments callbacks
    override fun requestGnss() {
        //code to request gnss
    }

    override fun onGpsStatusFragmentSet(gpsStatusFragment: GPSStatusFragment) {
        this.gpsStatusFragment = gpsStatusFragment
    }

    override fun onGalileoStatusFragmentSet(galileoStatusFragment: GalileoStatusFragment) {
        this.galileoStatusFragment = galileoStatusFragment
    }

    override fun onAllStatusFragmentSet(allStatusFragment: AllStatusFragment) {
        this.allStatusFragment = allStatusFragment
    }

    //callbacks
    override fun onLocationChanged(location: Location?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}
