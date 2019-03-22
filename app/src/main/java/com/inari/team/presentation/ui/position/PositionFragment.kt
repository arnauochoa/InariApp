package com.inari.team.presentation.ui.position

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.DataState.*
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.getModeIcon
import com.inari.team.core.utils.saveFile
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.toast
import com.inari.team.presentation.model.Mode
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.logs.LogsActivity
import com.inari.team.presentation.ui.main.MainActivity
import kotlinx.android.synthetic.main.dialog_save_log.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import javax.inject.Inject


class PositionFragment : BaseFragment(), OnMapReadyCallback, GnssEventsListener {

    @Inject
    lateinit var mSharedPreferences: AppSharedPreferences

    @Inject
    lateinit var navigator: Navigator

    companion object {
        const val FRAG_TAG = "position_fragment"
        const val SETTINGS_RESULT_CODE = 99

        const val SHOW_ALERT_ERROR = "show alert error"
        const val HIDE_ALERT_ERROR = "hide alert error"
    }

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mapFragment: SupportMapFragment? = null

    private var viewModel: PositionViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        viewModel = withViewModel(viewModelFactory) {
            observe(position, ::updatePosition)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)

        setHasOptionsMenu(true)
        setViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_position, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_log -> {
                showSaveDialog()
            }
            R.id.see_log -> {
                context?.let {
                    navigator.navigateToLogsActivity()
                }
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setViews() {
        fabOptions.setOnClickListener {
            navigator.navigateToModesActivity()
        }

        btComputeAction.setOnClickListener {
            if (btComputeAction.text == getString(R.string.start_computing)) {
                MainActivity.getInstance()?.subscribeToGnssEvents(this)
                btComputeAction.text = getString(R.string.stop_computing)

                val selectedModes = getSelectedModes()
                if (selectedModes.isEmpty()) { // If no constellation or band has been selected
                    showError("At least one Positioning Mode must be selected, ")
                } else {
                    viewModel?.setSelectedModes(selectedModes)
                    startPositioning()
                }
            } else {
                MainActivity.getInstance()?.unSubscribeToGnssEvent(this)
                btComputeAction.text = getString(R.string.start_computing)
                viewModel?.stopComputingPosition()
            }
        }

        initMap()
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

        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false


    }

    @SuppressLint("MissingPermission")
    private fun getSelectedModes(): List<Mode> {

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            location?.let {
                moveCamera(LatLng(location.latitude, location.longitude))
            }
        }

        return mSharedPreferences.getModesList().filter {
            it.isSelected
        }

    }

    private fun startPositioning() {
        mMap?.clear()
        showMapLoading()

        viewModel?.setStartTime()
        viewModel?.obtainEphemerisData()

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

    private fun moveCamera(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    private fun addMarker(latLng: LatLng, title: String, id: Int): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getModeIcon(id)))

        return mMap?.addMarker(markerOptions)
    }

    private fun showMapLoading() {
        pbMap?.visibility = View.VISIBLE
    }

    private fun hideMapLoading() {
        pbMap?.visibility = View.GONE
    }

    private fun showError(error: String) {
        activity?.runOnUiThread {
            toast(error)
        }
    }


    private fun updatePosition(data: Data<List<ResponsePvtMode>>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                    activity?.runOnUiThread {
                        showMapLoading()
                    }
                }
                SUCCESS -> {
                    it.data?.let { positions ->
                        positions.forEach { resp ->
                            addMarker(resp.position, "", resp.modeColor)
                        }
                        hideMapLoading()
                    }
                }
                ERROR -> {
                    hideMapLoading()
                    it.message?.let { msg ->
                        when (msg) {
                            SHOW_ALERT_ERROR -> {
                                ivAlert.visibility = VISIBLE
                            }
                            HIDE_ALERT_ERROR -> {
                                ivAlert.visibility = GONE
                            }
                            else -> showError(msg)

                        }

                    }
                }
            }
        }
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        viewModel?.setGnssStatus(status)
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        viewModel?.setGnssMeasurementsEvent(event)
    }

    override fun onLocationReceived(location: Location?) {
        viewModel?.setLocation(location)
    }

    override fun onGnssStarted() {
    }

    override fun onGnssStopped() {
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SETTINGS_RESULT_CODE -> {
                if (resultCode == RESULT_OK) {
                    //apply filters
                }
            }
        }
    }
}
