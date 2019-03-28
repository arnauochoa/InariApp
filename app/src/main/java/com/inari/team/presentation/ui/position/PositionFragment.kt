package com.inari.team.presentation.ui.position

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
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
import com.inari.team.core.utils.extensions.checkPermission
import com.inari.team.core.utils.extensions.withViewModel
import com.inari.team.core.utils.getModeIcon
import com.inari.team.core.utils.showAlert
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.main.MainListener
import kotlinx.android.synthetic.main.dialog_map_terrain.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import javax.inject.Inject


class PositionFragment : BaseFragment(), OnMapReadyCallback {

    @Inject
    lateinit var mSharedPreferences: AppSharedPreferences

    @Inject
    lateinit var navigator: Navigator

    private var mMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null

    private var viewModel: PositionViewModel? = null

    private var mainListener: MainListener? = null

    private var legendAdapter = LegendAdapter()

    private var isStartedComputing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        viewModel = withViewModel(viewModelFactory) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainListener = context as? MainListener
    }


    private fun setViews(view: View) {
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
                showAlert(
                    view.context, "Stop Computing",
                    "Are you sure that you want to stop computing position?", "YES", {
                        stopComputing()
                    }, true
                )
            }
        }

        ivLegendArrow.setOnClickListener {
            if (cvLegend.visibility == GONE) {
                cvLegend.visibility = VISIBLE
            } else cvLegend.visibility = GONE
            ivLegendArrow.rotation = ivLegendArrow.rotation + 180
        }

        rvLegend.layoutManager = LinearLayoutManager(view.context)
        rvLegend.adapter = legendAdapter

        initMap()
    }

    override fun onResume() {
        super.onResume()
        val legendItems = mSharedPreferences.getSelectedModesList()
        if (legendItems.isNotEmpty()) {
            legendAdapter.setItems(legendItems)
            cvLegendArrow.visibility = VISIBLE
            clLegend.visibility = VISIBLE
        } else {
            legendAdapter.clear()
            cvLegendArrow.visibility = GONE
            clLegend.visibility = GONE
        }
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


    @SuppressLint("MissingPermission")
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
            mMap?.clear()
            isStartedComputing = true
            btComputeAction.text = getString(R.string.stop_computing)
            mainListener?.startComputing(selectedModes)
        }

    }

    private fun stopComputing() {
        mainListener?.let {
            it.stopComputing()
            btComputeAction.text = getString(R.string.start_computing)
            hideMapLoading()
        }
    }

    //helpers
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

    private fun moveCamera(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun moveCameraWithZoom(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }

    private fun addMarker(latLng: LatLng, title: String, color: Int): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getModeIcon(color)))

        return mMap?.addMarker(markerOptions)
    }

    fun showMapLoading() {
        pbMap?.visibility = View.VISIBLE
    }

    fun hideMapLoading() {
        pbMap?.visibility = View.GONE
    }

    fun showEphemerisAlert(show: Boolean) {
        if (show) {
            ivAlert?.let {
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
            }
        } else {
            ivAlert.visibility = GONE
        }
    }

    //Callbacks
    fun onPositionsCalculated(positions: List<ResponsePvtMode>) {
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
        }
    }

    companion object {
        const val FRAG_TAG = "position_fragment"
    }
}
