package com.inari.team.ui

import android.annotation.SuppressLint
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
import com.inari.team.utils.BarAdapter
import com.inari.team.utils.context
import kotlinx.android.synthetic.main.activity_main.*
import java.text.ParsePosition

class MainActivity : AppCompatActivity(), LocationListener, PositionFragment.PositionListener {

    private var locationManager: LocationManager? = null
    private var locationProvider: LocationProvider? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var gnssMeasurementsEventListener: GnssMeasurementsEvent.Callback? = null
    private var gnssNavigationMessageListener: GnssNavigationMessage.Callback? = null

    private var positionFragment: PositionFragment? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setViewPager()
        setBottomNavigation()
        setGnssCallbacks()
    }

    //setters
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun setGnssCallbacks() {
        gnssStatusListener = object : GnssStatus.Callback() {
            override fun onStarted() {
                Toast.makeText(context, "GnssStatus.Callback onStarted()", Toast.LENGTH_SHORT).show()
            }

            override fun onStopped() {
                Toast.makeText(context, "GnssStatus.Callback onStopped()", Toast.LENGTH_SHORT).show()
            }

            override fun onFirstFix(ttffMillis: Int) {
                Toast.makeText(context, "GnssStatus.Callback onFirstFix()", Toast.LENGTH_SHORT).show()
            }

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                //once gnss status received, notice position fragment
                positionFragment?.onGnnsDataReceived(gnssStatus = status)

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
