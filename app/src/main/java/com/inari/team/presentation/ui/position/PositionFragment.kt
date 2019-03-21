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
import android.support.v4.content.ContextCompat
import android.view.*
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
import com.google.maps.android.ui.IconGenerator
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.DataState.*
import com.inari.team.core.utils.extensions.observe
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.saveFile
import com.inari.team.core.utils.skyplot.GnssEventsListener
import com.inari.team.core.utils.toast
import com.inari.team.presentation.model.PositionParameters
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
    }

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mapFragment: SupportMapFragment? = null

    private var viewModel: PositionViewModel? = null

    private var avgTime: Long = 5

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

                val selectedParameters = getSelectedParameters()
                if (selectedParameters.isEmpty()) { // If no constellation or band has been selected
                    //todo change message?
                    showError("At least one constellation and one band must be selected, go to settings to select one")
                } else {
                    viewModel?.setGnssData(parameters = selectedParameters)
                    startPositioning()
                }
            } else {
                MainActivity.getInstance()?.unSubscribeToGnssEvent(this)
                btComputeAction.text = getString(R.string.start_computing)
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

    private fun getSelectedParameters(): List<PositionParameters> {

        val positionParametersList = arrayListOf<PositionParameters>()

        //list with selected modes, get parameters from each mode
        val selectedModes = mSharedPreferences.getModesList().filter {
            it.isSelected
        }

        selectedModes.forEach {

            with(it) {
                positionParametersList.add(
                    PositionParameters(
                        constellations = constellations,
                        bands = bands,
                        corrections = corrections,
                        algorithm = algorithm
                    )
                )
                this@PositionFragment.avgTime = avgTime
            }

        }

        return positionParametersList
    }

    private fun startPositioning() {
        mMap?.clear()
        showMapLoading()

        viewModel?.setStartTime(avgTime)
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
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun addMarker(latLng: LatLng, title: String): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)

        context?.let {

            val iconGenerator = IconGenerator(it)
            iconGenerator.setBackground(ContextCompat.getDrawable(it, R.drawable.ic_pos))
            val bm = iconGenerator.makeIcon()
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bm))
        }


        return mMap?.addMarker(markerOptions)
    }

    private fun showMapLoading() {
        pbMap?.visibility = View.VISIBLE
    }

    private fun hideMapLoading() {
        pbMap?.visibility = View.GONE
    }

    private fun showError(error: String) {
        activity?.runOnUiThread { toast(error) }
    }


    private fun updatePosition(data: Data<LatLng>?) {
        data?.let {
            when (it.dataState) {
                LOADING -> {
                    activity?.runOnUiThread {
                        showMapLoading()
                    }
                }
                SUCCESS -> {
                    it.data?.let { position ->
                        hideMapLoading()
                        addMarker(position, "")
                        moveCamera(position)
                    }
                }
                ERROR -> {
                    hideMapLoading()
                    it.message?.let { msg -> showError(msg) }
                }
            }
        }
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        viewModel?.setGnssData(gnssStatus = status)
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        viewModel?.setGnssData(gnssMeasurementsEvent = event)
    }

    override fun onLocationReceived(location: Location?) {
        viewModel?.setGnssData(location = location)
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
