package com.inari.team.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.Surface
import android.widget.Toast
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.BarAdapter
import com.inari.team.core.utils.extensions.PERMISSION_ACCESS_FINE_LOCATION
import com.inari.team.core.utils.extensions.checkPermission
import com.inari.team.core.utils.extensions.checkPermissionsList
import com.inari.team.core.utils.extensions.requestPermissionss
import com.inari.team.core.utils.skyplot.GpsTestListener
import com.inari.team.core.utils.skyplot.GpsTestUtil
import com.inari.team.core.utils.skyplot.MathUtils
import com.inari.team.core.utils.toast
import com.inari.team.ui.position.PositionFragment
import com.inari.team.ui.statistics.StatisticsFragment
import com.inari.team.ui.status.StatusFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity(), LocationListener, SensorEventListener {

    companion object {
        private const val MIN_TIME = 1L
        private const val MIN_DISTANCE = 0.0F

        private var mActivity: MainActivity? = null

        fun getInstance(): MainActivity? {
            return mActivity
        }
    }

    private var locationManager: LocationManager? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var gnssMeasurementsEventListener: GnssMeasurementsEvent.Callback? = null

    private var positionFragment = PositionFragment()
    private var statusFragment = StatusFragment()
    private var statisticsFragment = StatisticsFragment()


    // Holds sensor data
    private val mRotationMatrix = FloatArray(16)

    private val mRemappedMatrix = FloatArray(16)

    private val mValues = FloatArray(3)

    private val mTruncatedRotationVector = FloatArray(4)

    private var mTruncateVector = false


    private var mFaceTrueNorth: Boolean = true


    private val mGpsTestListeners = ArrayList<GpsTestListener>()

    private var mGeomagneticField: GeomagneticField? = null

    private var mSensorManager: SensorManager? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityComponent.inject(this)

        mActivity = this

        setViewPager()
        setBottomNavigation()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        addOrientationSensorListener()

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
                    mGpsTestListeners.forEach {
                        it.onSatelliteStatusChanged(status)
                    }
                }
            }

            gnssMeasurementsEventListener = object : GnssMeasurementsEvent.Callback() {
                override fun onGnssMeasurementsReceived(measurementsEvent: GnssMeasurementsEvent?) {
                    positionFragment.onGnnsDataReceived(gnssMeasurementsEvent = measurementsEvent)
                }
            }

            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
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

    fun addListener(listener: GpsTestListener) {
        mGpsTestListeners.add(listener)
    }

    private fun addOrientationSensorListener() {
        if (GpsTestUtil.isRotationVectorSensorSupported(this)) {
            // Use the modern rotation vector sensors
            val vectorSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            mSensorManager?.registerListener(this, vectorSensor, 16000) // ~60hz
        } else {
            // Use the legacy orientation sensors
            val sensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            if (sensor != null) {
                mSensorManager?.registerListener(
                    this, sensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }
        }
    }

    //helpers
    private fun switchFragment(id: Int) {
        viewPager.setCurrentItem(id, false)
    }

    //callbacks
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    override fun onSensorChanged(event: SensorEvent) {

        var orientation: Double
        var tilt = java.lang.Double.NaN

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                // Modern rotation vector sensors
                if (!mTruncateVector) {
                    try {
                        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)
                    } catch (e: IllegalArgumentException) {
                        // On some Samsung devices, an exception is thrown if this vector > 4 (see #39)
                        // Truncate the array, since we can deal with only the first four values
                        mTruncateVector = true
                        // Do the truncation here the first time the exception occurs
                        getRotationMatrixFromTruncatedVector(event.values)
                    }

                } else {
                    // Truncate the array to avoid the exception on some devices (see #39)
                    getRotationMatrixFromTruncatedVector(event.values)
                }

                val rot = windowManager.defaultDisplay.rotation
                when (rot) {
                    Surface.ROTATION_0 ->
                        // No orientation change, use default coordinate system
                        SensorManager.getOrientation(mRotationMatrix, mValues)
                    Surface.ROTATION_90 -> {
                        // Log.d(TAG, "Rotation-90");
                        SensorManager.remapCoordinateSystem(
                            mRotationMatrix, SensorManager.AXIS_Y,
                            SensorManager.AXIS_MINUS_X, mRemappedMatrix
                        )
                        SensorManager.getOrientation(mRemappedMatrix, mValues)
                    }
                    Surface.ROTATION_180 -> {
                        // Log.d(TAG, "Rotation-180");
                        SensorManager
                            .remapCoordinateSystem(
                                mRotationMatrix, SensorManager.AXIS_MINUS_X,
                                SensorManager.AXIS_MINUS_Y, mRemappedMatrix
                            )
                        SensorManager.getOrientation(mRemappedMatrix, mValues)
                    }
                    Surface.ROTATION_270 -> {
                        // Log.d(TAG, "Rotation-270");
                        SensorManager
                            .remapCoordinateSystem(
                                mRotationMatrix, SensorManager.AXIS_MINUS_Y,
                                SensorManager.AXIS_X, mRemappedMatrix
                            )
                        SensorManager.getOrientation(mRemappedMatrix, mValues)
                    }
                    else ->
                        // This shouldn't happen - assume default orientation
                        SensorManager.getOrientation(mRotationMatrix, mValues)
                }// Log.d(TAG, "Rotation-0");
                // Log.d(TAG, "Rotation-Unknown");
                orientation = Math.toDegrees(mValues[0].toDouble())  // azimuth
                tilt = Math.toDegrees(mValues[1].toDouble())
            }
            Sensor.TYPE_ORIENTATION ->
                // Legacy orientation sensors
                orientation = event.values[0].toDouble()
            else ->
                // A sensor we're not using, so return
                return
        }

        // Correct for true north, if preference is set
        if (mFaceTrueNorth && mGeomagneticField != null) {
            orientation += mGeomagneticField?.declination?.toDouble() ?: 0.0
            // Make sure value is between 0-360
            orientation = MathUtils.mod(orientation.toFloat(), 360.0f).toDouble()
        }

        for (listener in mGpsTestListeners) {
            listener.onOrientationChanged(orientation, tilt)
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private fun getRotationMatrixFromTruncatedVector(vector: FloatArray) {
        System.arraycopy(vector, 0, mTruncatedRotationVector, 0, 4)
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, mTruncatedRotationVector)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


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
                toast(
                    "Location permissions are compulsory, please go to settings to enable.",
                    Toast.LENGTH_LONG
                )
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
