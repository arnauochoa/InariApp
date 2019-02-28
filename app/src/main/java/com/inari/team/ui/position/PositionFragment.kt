package com.inari.team.ui.position

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import com.inari.team.data.NavigationMessage
import com.inari.team.data.PositionParameters
import com.inari.team.ui.logs.LogsActivity
import com.inari.team.utils.AppSharedPreferences
import com.inari.team.utils.context
import com.inari.team.utils.saveFile
import com.inari.team.utils.toast
import kotlinx.android.synthetic.main.dialog_save_log.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import kotlinx.android.synthetic.main.view_bottom_sheet.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToLong


class PositionFragment : Fragment(), OnMapReadyCallback, PositionView {

    companion object {
        const val FRAG_TAG = "position_fragment"
        const val SUPL_SERVER_HOST = "supl.google.com"
        const val SUPL_SERVER_PORT = 7275
        const val SUPL_SSL_ENABLED = true
        const val SUPL_MESSAGE_LOGGING_ENABLED = true
        const val SUPL_LOGGING_ENABLED = true
    }

    private val mSharedPreferences = AppSharedPreferences.getInstance()

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mZoom = 0.30
    private var mapFragment: SupportMapFragment? = null

    private var mListener: PositionListener? = null

    private var mPresenter: PositionPresenter? = null

    private var suplController: SuplController? = null

    private var refPos: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = PositionPresenter(this)

        buildSuplController()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)
        setHasOptionsMenu(true)

        fabOptions.setOnClickListener {
            clBottomSheet.visibility = View.VISIBLE
        }

        fabClose.setOnClickListener {
            if (clBottomSheet.visibility == View.VISIBLE) {
                val selectedParameters = getSelectedParameters()
                if (selectedParameters == null) { // If no constellation or band has been selected
                    toast("At least one constellation and one band must be selected")
                } else {
                    clBottomSheet.visibility = View.GONE
                    mMap?.clear()
                    showMapLoading()
                    obtainEphemerisData()
                    mPresenter?.setGnssData(selectedParameters)
                    mPresenter?.calculatePositionWithGnss()
                }
            }
        }

    }

    private fun buildSuplController() {
        val request = SuplConnectionRequest.builder()
            .setServerHost(SUPL_SERVER_HOST)
            .setServerPort(SUPL_SERVER_PORT)
            .setSslEnabled(SUPL_SSL_ENABLED)
            .setMessageLoggingEnabled(SUPL_MESSAGE_LOGGING_ENABLED)
            .setLoggingEnabled(SUPL_LOGGING_ENABLED)
            .build()
        suplController = SuplController(request)

    }

    private fun obtainEphemerisData() {
        var ephResponse: EphemerisResponse? = null
        refPos?.let {
            val latE7 = (it.latitude * 1e7).roundToLong()
            val lngE7 = (it.longitude * 1e7).roundToLong()

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
            suplController?.sendSuplRequest(latE7, lngE7)
            ephResponse = suplController?.generateEphResponse(latE7, lngE7)
            mPresenter?.setGnssData(ephemerisResponse = ephResponse)
        }
        if (ephResponse == null) {
            toast("Ephemeris data could not be obtained")
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
        if (correctionsParam3.isChecked) corrections.add(PositionParameters.CORR_MULTIPATH)
        if (correctionsParam4.isChecked) corrections.add(PositionParameters.CORR_CAMERA)
        if (algorithmParam1.isChecked) algorithm = PositionParameters.ALG_LS  // set selected algorithm
        if (algorithmParam2.isChecked) algorithm = PositionParameters.ALG_WLS
        if (algorithmParam3.isChecked) algorithm = PositionParameters.ALG_KALMAN

        return if (constellations.isEmpty() || bands.isEmpty()) {
            null
        } else {
            PositionParameters(constellations, bands, corrections, algorithm)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = context as? PositionListener
    }

    override fun onResume() {
        super.onResume()
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

    private fun showSaveDialog() {
        context?.let {
            val dialog = AlertDialog.Builder(it).create()
            val layout = View.inflate(it, R.layout.dialog_save_log, null)
            dialog.window?.let { wind ->
                wind.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
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
                } else toast("File name can not be empty")
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

    fun showMapLoading() {
        pbMap?.visibility = View.VISIBLE
    }

    fun hideMapLoading() {
        pbMap?.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onGnnsDataReceived(
        location: Location? = null,
        gnssStatus: GnssStatus? = null,
        gnssMeasurementsEvent: GnssMeasurementsEvent? = null,
        gnssNavigationMessages: HashMap<Int, NavigationMessage>? = null
    ) {
        location?.let {
            refPos = LatLng(location.latitude, location.longitude)
        }
        mPresenter?.setGnssData(
            location = location,
            gnssStatus = gnssStatus,
            gnssMeasurementsEvent = gnssMeasurementsEvent,
            gnssNavigationMessages = gnssNavigationMessages
        )
    }

    override fun onPositionCalculated(position: LatLng) {
        //toast("Position computed!")
        hideMapLoading()
        addMarker(position, "")
        moveCamera(position)
        //position obtained
    }

    override fun onPositionNotCalculated() {
        toast("There are not enough measurements yet.")
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

    interface PositionListener {
        fun requestGnss()
    }

}
