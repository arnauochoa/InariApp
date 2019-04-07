package com.inari.team.presentation.ui.maplog

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.R
import com.inari.team.core.base.BaseActivity
import com.inari.team.core.utils.getModeIcon
import com.inari.team.core.utils.retrievePositionsFile
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.position.PositionFragment
import kotlinx.android.synthetic.main.activity_map_log.*
import kotlinx.android.synthetic.main.dialog_map_terrain.view.*


class MapLogActivity : BaseActivity(), OnMapReadyCallback {

    private var legendAdapter = MapLogLegendAdapter()

    private var mapFragment: SupportMapFragment? = null

    private val positions = arrayListOf<ResponsePvtMode>()
    private var mMap: GoogleMap? = null

    private var mZoom = 0.2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_log)
        activityComponent.inject(this)

        val fileName = intent.getStringExtra(POSITIONS_EXTRA) ?: ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = fileName

        val fileContent = retrievePositionsFile(fileName)

        val type = object : TypeToken<ArrayList<ResponsePvtMode>>() {}.type

        val pos = Gson().fromJson<ArrayList<ResponsePvtMode>>(fileContent, type) ?: arrayListOf()
        positions.addAll(pos)

        initMap()

        setViews()
    }

    private fun setViews() {
        fabMapTerrain.setOnClickListener {
            showMapTypeDialog()
        }

        ivLegendArrow.setOnClickListener {
            if (cvLegend.visibility == GONE) {
                cvLegend.visibility = VISIBLE
            } else cvLegend.visibility = GONE
            ivLegendArrow.rotation = ivLegendArrow.rotation + 180
        }

        rvLegend.layoutManager = LinearLayoutManager(this)
        rvLegend.adapter = legendAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun initMap() {
        supportFragmentManager.let {
            mapFragment =
                (it.findFragmentByTag(PositionFragment.FRAG_TAG) as? SupportMapFragment) ?: SupportMapFragment()
            mapFragment?.let { map ->
                it.beginTransaction()
                    .replace(
                        R.id.mapFragmentContainer,
                        map,
                        PositionFragment.FRAG_TAG
                    )
                    .commit()
                it.executePendingTransactions()

                map.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        val builder = LatLngBounds.Builder()

        val width: Int = resources.displayMetrics.widthPixels
        val padding: Int = (width * mZoom).toInt() // offset from edges of the map 10% of screen

        val legendItems = hashMapOf<Int, String>()
        positions.forEach {
            addMarker(LatLng(it.pvtLatLng.lat, it.pvtLatLng.lng), it.modeName, it.modeColor)
            builder.include(LatLng(it.pvtLatLng.lat, it.pvtLatLng.lng))
            if (!legendItems.keys.contains(it.modeColor)) {
                legendItems[it.modeColor] = it.modeName
            }

        }

        addLegendItems(legendItems)

        mapFragmentContainer?.viewTreeObserver?.addOnGlobalLayoutListener {

            if (positions.isNotEmpty()) {
                val bounds = builder.build()
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                try {
                    mMap?.animateCamera(cu)
                } catch (e: Exception) {

                }
            }
        }

    }

    private fun addLegendItems(legendItems: HashMap<Int, String>) {
        val mapLogLegendItems = arrayListOf<MapLogLegendAdapter.MapLogLegendItem>()
        legendItems.forEach { color, name ->
            mapLogLegendItems.add(MapLogLegendAdapter.MapLogLegendItem(color, name))
        }

        if (mapLogLegendItems.isNotEmpty()) {
            legendAdapter.setItems(mapLogLegendItems)
            cvLegendArrow.visibility = VISIBLE
            clLegend.visibility = VISIBLE
        } else {
            legendAdapter.clear()
            cvLegendArrow.visibility = GONE
            clLegend.visibility = GONE
        }
    }

    private fun addMarker(latLng: LatLng, title: String, color: Int): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getModeIcon(color)))

        return mMap?.addMarker(markerOptions)
    }

    //helpers
    private fun showMapTypeDialog() {

        val dialog = AlertDialog.Builder(this).create()
        val layout = View.inflate(this, R.layout.dialog_map_terrain, null)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(layout)

        layout?.let { view ->

            mMap?.let { gMap ->
                when (gMap.mapType) {
                    GoogleMap.MAP_TYPE_NORMAL -> {
                        view.ivNormalTick.visibility = View.VISIBLE
                        view.ivTerrainTick.visibility = View.GONE
                        view.ivHybridTick.visibility = View.GONE
                        view.ivSatelliteTick.visibility = View.GONE
                    }
                    GoogleMap.MAP_TYPE_TERRAIN -> {
                        view.ivTerrainTick.visibility = View.VISIBLE
                        view.ivNormalTick.visibility = View.GONE
                        view.ivHybridTick.visibility = View.GONE
                        view.ivSatelliteTick.visibility = View.GONE
                    }
                    GoogleMap.MAP_TYPE_HYBRID -> {
                        view.ivHybridTick.visibility = View.VISIBLE
                        view.ivNormalTick.visibility = View.GONE
                        view.ivTerrainTick.visibility = View.GONE
                        view.ivSatelliteTick.visibility = View.GONE
                    }
                    GoogleMap.MAP_TYPE_SATELLITE -> {
                        view.ivSatelliteTick.visibility = View.VISIBLE
                        view.ivHybridTick.visibility = View.GONE
                        view.ivNormalTick.visibility = View.GONE
                        view.ivTerrainTick.visibility = View.GONE
                    }
                }
            }

            view.clNormal.setOnClickListener {
                view.ivNormalTick.visibility = View.VISIBLE
                view.ivTerrainTick.visibility = View.GONE
                view.ivHybridTick.visibility = View.GONE
                view.ivSatelliteTick.visibility = View.GONE

                mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                dialog.dismiss()
            }
            view.clTerrain.setOnClickListener {
                view.ivTerrainTick.visibility = View.VISIBLE
                view.ivNormalTick.visibility = View.GONE
                view.ivHybridTick.visibility = View.GONE
                view.ivSatelliteTick.visibility = View.GONE

                mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                dialog.dismiss()
            }
            view.clHybrid.setOnClickListener {
                view.ivHybridTick.visibility = View.VISIBLE
                view.ivNormalTick.visibility = View.GONE
                view.ivTerrainTick.visibility = View.GONE
                view.ivSatelliteTick.visibility = View.GONE

                mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                dialog.dismiss()
            }
            view.clSatellite.setOnClickListener {
                view.ivSatelliteTick.visibility = View.VISIBLE
                view.ivHybridTick.visibility = View.GONE
                view.ivNormalTick.visibility = View.GONE
                view.ivTerrainTick.visibility = View.GONE

                mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                dialog.dismiss()
            }

        }
        dialog.show()


    }

    companion object {
        const val POSITIONS_EXTRA: String = "positions"
    }
}
