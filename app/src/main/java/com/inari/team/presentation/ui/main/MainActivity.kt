package com.inari.team.presentation.ui.main

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.*
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Surface
import android.widget.Toast
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.BarAdapter
import com.inari.team.core.utils.extensions.*
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.skyplot.GpsTestUtil
import com.inari.team.core.utils.skyplot.MathUtils
import com.inari.team.core.utils.toast
import com.inari.team.core.utils.view.CustomAHBottomNavigationItem
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.about.AboutFragment
import com.inari.team.presentation.ui.logs.LogsFragment
import com.inari.team.presentation.ui.position.PositionFragment
import com.inari.team.presentation.ui.splash.SplashActivity
import com.inari.team.presentation.ui.statistics.StatisticsFragment
import com.inari.team.presentation.ui.status.StatusFragment
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity(), MainListener, LocationListener, SensorEventListener {

    companion object {
        private const val MIN_TIME = 1L
        private const val MIN_DISTANCE = 0.0F
        const val TUTORIAL_CODE = 88

        private var mActivity: MainActivity? = null

        fun getInstance(): MainActivity? {
            return mActivity
        }
    }

    @Inject
    lateinit var mPrefs: AppSharedPreferences

    @Inject
    lateinit var navigator: Navigator

    private var viewModel: MainViewModel? = null

    private var locationManager: LocationManager? = null

    private var gnssStatusListener: GnssStatus.Callback? = null
    private var gnssMeasurementsEventListener: GnssMeasurementsEvent.Callback? = null
    private var gnssNmeaMessageListener: OnNmeaMessageListener? = null

    private val logsFragment = LogsFragment()

    private var gnssListeners = arrayListOf<GnssEventsListener>()

    // Holds sensor data
    private val mRotationMatrix = FloatArray(16)
    private val mRemappedMatrix = FloatArray(16)
    private val mValues = FloatArray(3)
    private val mTruncatedRotationVector = FloatArray(4)
    private var mTruncateVector = false
    private var mFaceTrueNorth: Boolean = true
    private var mGeomagneticField: GeomagneticField? = null
    private var mSensorManager: SensorManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityComponent.inject(this)

        viewModel = withViewModel(viewModelFactory){
//            observe(position, ::updatePosition)
//            observe(ephemeris, ::updateEphemeris)
//            observe(saveLogs, ::updateSavedLogs)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_logo_small)

        if (!mPrefs.isTutorialShown()) {
            navigator.navigateToTutorialActivtiy()
        }

        mActivity = this

        setViewPager()
        setupBottomNavigation()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        addOrientationSensorListener()

        startGnss()
    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(supportFragmentManager)

        pagerAdapter.addFragments(PositionFragment(), "Position")
        pagerAdapter.addFragments(StatusFragment(), "GNSS state")
        pagerAdapter.addFragments(StatisticsFragment(), "Statistics")
        pagerAdapter.addFragments(logsFragment, "Logs")
        pagerAdapter.addFragments(AboutFragment(), "About")

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 5
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 0
    }

    private fun setupBottomNavigation() {

        val position = CustomAHBottomNavigationItem(getString(R.string.position_bottom), R.drawable.ic_position)
        val status = CustomAHBottomNavigationItem(getString(R.string.gnss_state_bottom), R.drawable.ic_satellite)
        val statistics = CustomAHBottomNavigationItem(getString(R.string.statistics_bottom), R.drawable.ic_statistics)
        val logs = CustomAHBottomNavigationItem(getString(R.string.logs_bottom), R.drawable.ic_file)
        val info = CustomAHBottomNavigationItem(getString(R.string.about_bottom), R.drawable.ic_info)


        val itemList = arrayListOf(position, status, statistics, logs, info)
        bottomNavigation.addItems(itemList)
        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        bottomNavigation.defaultBackgroundColor = ContextCompat.getColor(this, R.color.white)
        bottomNavigation.accentColor = ContextCompat.getColor(this, R.color.colorPrimary)

        bottomNavigation.setOnTabSelectedListener { pos, wasSelected ->
            if (!wasSelected) {
                viewPager.setCurrentItem(pos, false)
            }
            true
        }

    }

    private fun startGnss() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            gnssStatusListener = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    gnssListeners.forEach {
                        it.onSatelliteStatusChanged(status)
                    }
                }

                override fun onStarted() {
                    super.onStarted()
                    gnssListeners.forEach {
                        it.onGnssStarted()
                    }
                }

                override fun onStopped() {
                    super.onStopped()
                    gnssListeners.forEach {
                        it.onGnssStopped()
                    }
                }
            }

            gnssMeasurementsEventListener = object : GnssMeasurementsEvent.Callback() {
                override fun onGnssMeasurementsReceived(measurementsEvent: GnssMeasurementsEvent?) {
                    gnssListeners.forEach {
                        it.onGnssMeasurementsReceived(measurementsEvent)
                    }
                }
            }

            gnssNmeaMessageListener = OnNmeaMessageListener { message, timestamp ->
                gnssListeners.forEach {
                    it.onNmeaMessageReceived(message, timestamp)
                }
            }

            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE, this
            )
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE, this
            )

            locationManager?.registerGnssStatusCallback(gnssStatusListener)
            locationManager?.registerGnssMeasurementsCallback(gnssMeasurementsEventListener)
            locationManager?.addNmeaListener(gnssNmeaMessageListener)

        } else {
            requestPermissionss(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), SplashActivity.PERMISSIONS_CODE
            )
        }
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

    fun subscribeToGnssEvents(listener: GnssEventsListener) {
        gnssListeners.add(listener)
    }

    fun unSubscribeToGnssEvent(listener: GnssEventsListener) {
        gnssListeners.remove(listener)
    }

    fun navigateToLogs() {
        viewPager.currentItem = 3
        bottomNavigation.currentItem = 3
        logsFragment.setFiles()
    }


//    //Callbacks
//    private fun updatePosition(data: Data<List<ResponsePvtMode>>?) {
//        data?.let {
//            when (it.dataState) {
//                DataState.LOADING -> {
//                    showMapLoading()
//                }
//                DataState.SUCCESS -> {
//                    hideMapLoading()
//                    it.data?.let { positions ->
//                        if (positions.isNotEmpty()) {
//                            positions.forEach { resp ->
//                                addMarker(resp.position, "", resp.modeColor)
//                            }
//                            if (isStartedComputing) {
//                                moveCameraWithZoom(positions[0].position)
//                                isStartedComputing = false
//                            } else {
//                                moveCamera(positions[0].position)
//                            }
//                            positionsList.addAll(positions)
//                        }
//                    }
//                }
//                DataState.ERROR -> {
//                    hideMapLoading()
//                    it.message?.let { msg ->
//                        showError(msg)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun updateEphemeris(data: Data<String>?) {
//        data?.let {
//            when (it.dataState) {
//                DataState.LOADING -> {
//                }
//                DataState.SUCCESS -> {
//                    showEphemerisAlert(false)
//                }
//                DataState.ERROR -> {
//                    showEphemerisAlert(true)
//                }
//            }
//        }
//    }
//
//    private fun updateSavedLogs(data: Data<Any>?) {
//        data?.let {
//            when (data.dataState) {
//                DataState.LOADING -> {
//                }
//                DataState.SUCCESS -> {
//                    showSavedSnackBar()
//                }
//                DataState.ERROR -> {
//                    showError("An error occurred saving logs")
//                }
//            }
//        }
//    }

    override fun startComputing() {

    }

    override fun stopComputing() {

    }


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

        gnssListeners.forEach {
            it.onOrientationChanged(orientation, tilt)
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private fun getRotationMatrixFromTruncatedVector(vector: FloatArray) {
        System.arraycopy(vector, 0, mTruncatedRotationVector, 0, 4)
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, mTruncatedRotationVector)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onLocationChanged(location: Location?) {
        gnssListeners.forEach {
            it.onLocationReceived(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TUTORIAL_CODE -> {
                navigator.navigateToMainActivity()
                mPrefs.setTutorialShown()
            }
        }
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
        gnssListeners.clear()
    }
}

interface MainListener {
    fun startComputing()
    fun stopComputing()
}
