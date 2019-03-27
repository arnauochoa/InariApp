package com.inari.team.presentation.ui.position

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
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
import com.inari.team.core.utils.extensions.checkPermission
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.getModeIcon
import com.inari.team.core.utils.showAlert
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.toast
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.main.MainActivity
import kotlinx.android.synthetic.main.dialog_map_terrain.view.*
import kotlinx.android.synthetic.main.dialog_save_log.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import javax.inject.Inject


class PositionFragment : BaseFragment(), OnMapReadyCallback, GnssEventsListener {

    @Inject
    lateinit var mSharedPreferences: AppSharedPreferences

    @Inject
    lateinit var navigator: Navigator

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mapFragment: SupportMapFragment? = null

    private var positionsList = arrayListOf<ResponsePvtMode>()

    private var viewModel: PositionViewModel? = null

    private var isStartedComputing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        viewModel = withViewModel(viewModelFactory) {
            observe(position, ::updatePosition)
            observe(ephemeris, ::updateEphemeris)
            observe(saveLogs, ::updateSavedLogs)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)

        setViews()
    }


    private fun setViews() {
        fabOptions.setOnClickListener {
            navigator.navigateToModesActivity()
        }

        fabMapTerrain.setOnClickListener {
            showMapTypeDialog()
        }

        btComputeAction.setOnClickListener {
            if (btComputeAction.text == getString(R.string.start_computing)) {
                startComputing(it)
            } else {
                stopComputing()
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
        mMap = map

        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap?.isMyLocationEnabled = true
        }
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.mapType = mSharedPreferences.getSelectedMapType()
    }

    private fun startComputing(it: View) {
        val selectedModes = mSharedPreferences.getSelectedModesList()
        if (selectedModes.isEmpty()) { // If no constellation or band has been selected
            showAlert(
                it.context,
                "Select Parameters",
                "At least one Positioning Mode must be selected",
                "go to settings",
                positiveAction = {
                    navigator.navigateToModesActivity()

                },
                isCancelable = true
            )
        } else {
            isStartedComputing = true
            MainActivity.getInstance()?.subscribeToGnssEvents(this)
            btComputeAction.text = getString(R.string.stop_computing)
            viewModel?.setSelectedModes(selectedModes)
            startPositioning()
        }
    }

    private fun startPositioning() {
        mMap?.clear()
        viewModel?.startComputingPosition()
        viewModel?.obtainEphemerisData()

    }

    private fun stopComputing() {
        viewModel?.stopComputingPosition()
        MainActivity.getInstance()?.unSubscribeToGnssEvent(this)
        btComputeAction.text = getString(R.string.start_computing)
        showSaveDialog()
        hideMapLoading()
    }

    private fun showMapTypeDialog() {

        context?.let { c ->
            val dialog = AlertDialog.Builder(c).create()
            val layout = View.inflate(c, R.layout.dialog_map_terrain, null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(layout)

            layout?.let { view ->

                mMap?.let { gMap ->
                    when (gMap.mapType) {
                        GoogleMap.MAP_TYPE_NORMAL -> {
                            view.ivNormalTick.visibility = VISIBLE
                            view.ivTerrainTick.visibility = GONE
                            view.ivHybridTick.visibility = GONE
                            view.ivSatelliteTick.visibility = GONE
                        }
                        GoogleMap.MAP_TYPE_TERRAIN -> {
                            view.ivTerrainTick.visibility = VISIBLE
                            view.ivNormalTick.visibility = GONE
                            view.ivHybridTick.visibility = GONE
                            view.ivSatelliteTick.visibility = GONE
                        }
                        GoogleMap.MAP_TYPE_HYBRID -> {
                            view.ivHybridTick.visibility = VISIBLE
                            view.ivNormalTick.visibility = GONE
                            view.ivTerrainTick.visibility = GONE
                            view.ivSatelliteTick.visibility = GONE
                        }
                        GoogleMap.MAP_TYPE_SATELLITE -> {
                            view.ivSatelliteTick.visibility = VISIBLE
                            view.ivHybridTick.visibility = GONE
                            view.ivNormalTick.visibility = GONE
                            view.ivTerrainTick.visibility = GONE
                        }
                    }
                }

                view.clNormal.setOnClickListener {
                    view.ivNormalTick.visibility = VISIBLE
                    view.ivTerrainTick.visibility = GONE
                    view.ivHybridTick.visibility = GONE
                    view.ivSatelliteTick.visibility = GONE

                    mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    mSharedPreferences.setSelectedMapType(GoogleMap.MAP_TYPE_NORMAL)
                    dialog.dismiss()
                }
                view.clTerrain.setOnClickListener {
                    view.ivTerrainTick.visibility = VISIBLE
                    view.ivNormalTick.visibility = GONE
                    view.ivHybridTick.visibility = GONE
                    view.ivSatelliteTick.visibility = GONE

                    mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    mSharedPreferences.setSelectedMapType(GoogleMap.MAP_TYPE_TERRAIN)
                    dialog.dismiss()
                }
                view.clHybrid.setOnClickListener {
                    view.ivHybridTick.visibility = VISIBLE
                    view.ivNormalTick.visibility = GONE
                    view.ivTerrainTick.visibility = GONE
                    view.ivSatelliteTick.visibility = GONE

                    mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                    mSharedPreferences.setSelectedMapType(GoogleMap.MAP_TYPE_HYBRID)
                    dialog.dismiss()
                }
                view.clSatellite.setOnClickListener {
                    view.ivSatelliteTick.visibility = VISIBLE
                    view.ivHybridTick.visibility = GONE
                    view.ivNormalTick.visibility = GONE
                    view.ivTerrainTick.visibility = GONE

                    mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    mSharedPreferences.setSelectedMapType(GoogleMap.MAP_TYPE_SATELLITE)
                    dialog.dismiss()
                }

            }
            dialog.show()
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
                    viewModel?.saveLastLogs(fileName + format)
                    dialog.dismiss()
                    positionsList.clear()
                } else showError("File name can not be empty")
            }
            layout.tvDiscard.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    private fun showSavedSnackBar() {
        val snackbar = Snackbar.make(snackbarCl, "File saved", Snackbar.LENGTH_LONG)
        snackbar.setAction("OPEN") {
            MainActivity.getInstance()?.navigateToLogs()
        }
        snackbar.show()
    }

    private fun moveCamera(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun moveCameraWithZoom(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
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
        toast(error)
    }

    private fun showEphemerisAlert(show: Boolean) {
        if (show) {
            if (ivAlert.visibility != VISIBLE) {
                ivAlert.visibility = VISIBLE
                ivAlert.setOnClickListener {
                    showAlert(
                        ivAlert.context, "Something went wrong",
                        "Ephemeris data could not be obtained, check your connection",
                        "Stop Computing", {
                            stopComputing()
                        }, true
                    )
                }
            }
        } else {
            ivAlert.visibility = GONE
        }
    }


    private fun updatePosition(data: Data<List<ResponsePvtMode>>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                    showMapLoading()
                }
                SUCCESS -> {
                    hideMapLoading()
                    it.data?.let { positions ->
                        if (positions.isNotEmpty()) {
                            positions.forEach { resp ->
                                addMarker(resp.position, "", resp.modeColor)
                            }
                            if (isStartedComputing) {
                                moveCameraWithZoom(positions[0].position)
                                isStartedComputing = false
                            } else {
                                moveCamera(positions[0].position)
                            }
                            positionsList.addAll(positions)
                        }
                    }
                }
                ERROR -> {
                    hideMapLoading()
                    it.message?.let { msg ->
                        showError(msg)
                    }
                }
            }
        }
    }

    private fun updateEphemeris(data: Data<String>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                }
                SUCCESS -> {
                    showEphemerisAlert(false)
                }
                ERROR -> {
                    showEphemerisAlert(true)
                }
            }
        }
    }

    private fun updateSavedLogs(data: Data<Any>?) {
        data?.let {
            when (data.dataState) {
                LOADING -> {
                }
                SUCCESS -> {
                    showSavedSnackBar()
                }
                ERROR -> {
                    showError("An error occurred saving logs")
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

    companion object {
        const val FRAG_TAG = "position_fragment"

        const val SHOW_ALERT_ERROR = "show alert error"
        const val HIDE_ALERT_ERROR = "hide alert error"
    }
}
