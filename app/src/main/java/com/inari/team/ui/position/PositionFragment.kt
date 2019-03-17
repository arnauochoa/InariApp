package com.inari.team.ui.position

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.view.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.location.suplclient.ephemeris.EphemerisResponse
import com.google.location.suplclient.supl.SuplConnectionRequest
import com.google.location.suplclient.supl.SuplController
import com.inari.team.R
import com.inari.team.data.PositionParameters
import com.inari.team.ui.logs.LogsActivity
import com.inari.team.utils.AppSharedPreferences
import com.inari.team.utils.context
import com.inari.team.utils.saveFile
import com.inari.team.utils.toast
import kotlinx.android.synthetic.main.dialog_save_log.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import kotlinx.android.synthetic.main.view_bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody
import kotlin.math.roundToLong


class PositionFragment : Fragment(), OnMapReadyCallback, PositionView {

    companion object {
        const val FRAG_TAG = "position_fragment"
    }

    private val mSharedPreferences = AppSharedPreferences.getInstance()

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mapFragment: SupportMapFragment? = null

    private var mPresenter: PositionPresenter? = null

    private var avgTime: Long = 5

    private var isPositioningStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setViews()

        mPresenter = PositionPresenter(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_position, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_log -> {
                showSaveDialog()
            }
            R.id.see_log -> {
                context?.let {
                    startActivity(Intent(it, LogsActivity::class.java))
                }
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViews() {
        fabOptions.setOnClickListener {
            clBottomSheet.visibility = View.VISIBLE
        }

        fabClose.setOnClickListener {
            val selectedParameters = getSelectedParameters()
            if (selectedParameters == null) { // If no constellation or band has been selected
                    showError("At least one constellation and one band must be selected")
            } else {
                isPositioningStarted = true
                mPresenter?.setGnssData(parameters = selectedParameters)
                startPositioning()
            }

        }

        //todo change when start and stop button implemented
        Handler().postDelayed({
            initMap()
        }, 1000)
    }

    private fun initMap() {
        childFragmentManager.let {
            mapFragment = (it.findFragmentByTag(FRAG_TAG) as? SupportMapFragment) ?: SupportMapFragment()
            mapFragment?.let { map ->
                it.beginTransaction()
                    .replace(
                        R.id.mapFragmentContainer,
                        map,
                        FRAG_TAG
                    )
                    .commit()
                it.executePendingTransactions()

                map.getMapAsync(this@PositionFragment)
            }
        }
    }


    override fun onMapReady(map: GoogleMap?) {
        hideMapLoading()
        setMap(map)
    }

    @SuppressLint("MissingPermission")
    fun setMap(map: GoogleMap?) {

        mMap = map
        mMap?.setMaxZoomPreference(15f)

        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false

        //requests the current user location
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                animateCamera(latLng)
                addMarker(latLng, "This is the obtained location")
            }
        }

    }

    private fun getSelectedParameters(): PositionParameters? {
        val constellations = arrayListOf<String>()
        val bands = arrayListOf<String>()
        val corrections = arrayListOf<String>()
        var algorithm: String? = null

        if (constParam1.isChecked) constellations.add(PositionParameters.CONST_GPS) // set selected constellations
        if (constParam2.isChecked) constellations.add(PositionParameters.CONST_GAL)
        if (bandsParam1.isChecked) bands.add(PositionParameters.BAND_L1) // set selected bands
        if (bandsParam2.isChecked) bands.add(PositionParameters.BAND_L5)
        if (correctionsParam1.isChecked) corrections.add(PositionParameters.CORR_IONOSPHERE)  // set selected corrections
        if (correctionsParam2.isChecked) corrections.add(PositionParameters.CORR_TROPOSPHERE)
        if (algorithmParam1.isChecked) algorithm = PositionParameters.ALG_LS  // set selected algorithm
        if (algorithmParam2.isChecked) algorithm = PositionParameters.ALG_WLS
        if (algorithmParam3.isChecked) algorithm = PositionParameters.ALG_KALMAN
        if (averagingParam1.isChecked) avgTime = PositionParameters.AVERAGING_TIME_SEC_1 // set selected averaging time
        if (averagingParam2.isChecked) avgTime = PositionParameters.AVERAGING_TIME_SEC_2
        if (averagingParam3.isChecked) avgTime = PositionParameters.AVERAGING_TIME_SEC_3

        return if (constellations.isEmpty() || bands.isEmpty()) {
            null
        } else {
            PositionParameters(constellations, bands, corrections, algorithm)
        }
    }

    private fun startPositioning() {
        clBottomSheet.visibility = View.GONE
        mMap?.clear()
        showMapLoading()

        GlobalScope.launch {
            mPresenter?.setStartTime(avgTime)
            mPresenter?.obtainEphemerisData()
        }
    }

    private fun showSaveDialog() {
        context?.let {
            val dialog = AlertDialog.Builder(it).create()
            val layout = View.inflate(it, R.layout.dialog_save_log, null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(layout)
            layout.save.setOnClickListener {
                val fileName = layout.fileName.text.toString()
                if (fileName.isNotBlank()) {
                    var format = ".rnx"
                    if (layout.radioGroupFormat.checkedRadioButtonId != R.id.rinex) {
                        format = ".nma"
                    }
                    saveLog(fileName + format)
                    dialog.dismiss()
                } else showError("File name can not be empty")
            }
            dialog.show()
        }
    }

    private fun saveLog(fileName: String) {
        val pvtInfoString = mSharedPreferences.getData(AppSharedPreferences.PVT_INFO)

        pvtInfoString?.let {
            saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), it))
            showSavedSnackBar()
        }
    }

    private fun showSavedSnackBar() {
        val snackbar = Snackbar.make(snackbarCl, "File saved", Snackbar.LENGTH_LONG)
        snackbar.setAction("OPEN") {
            context?.let {
                startActivity(Intent(it, LogsActivity::class.java))
            }
        }
        snackbar.show()
    }

    private fun animateCamera(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun moveCamera(latLng: LatLng) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun addMarker(latLng: LatLng, title: String): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        return mMap?.addMarker(markerOptions)
    }


    fun onGnnsDataReceived(
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null
    ) {
        if (isPositioningStarted) {
            mPresenter?.setGnssData(
                location = location,
                gnssStatus = gnssStatus,
                gnssMeasurementsEvent = gnssMeasurementsEvent
            )
        }
    }

    override fun onPositionCalculated(position: LatLng) {
        activity?.runOnUiThread {
            hideMapLoading()
            addMarker(position, "")
            moveCamera(position)
        }
        //position obtained
    }

    override fun showMapLoading() {
        pbMap?.visibility = View.VISIBLE
    }

    override fun hideMapLoading() {
        pbMap?.visibility = View.GONE
    }

    override fun showError(error: String) {
        activity?.runOnUiThread { toast(error) }
    }

    override fun showMessage(message: String) {
        activity?.runOnUiThread { toast(message) }
    }
}
