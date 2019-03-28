package com.inari.team.presentation.ui.maplog

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.R
import com.inari.team.core.utils.retrievePositionsFile
import com.inari.team.presentation.model.ResponsePvtMode
import com.inari.team.presentation.ui.position.PositionFragment

class MapLogActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapFragment: SupportMapFragment? = null

    private val positions = arrayListOf<ResponsePvtMode>()
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_log)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fileName = intent.getStringExtra(POSITIONS_EXTRA) ?: ""

        val fileContent = retrievePositionsFile(fileName)

        val type = object : TypeToken<ArrayList<ResponsePvtMode>>() {}.type

        val pos = Gson().fromJson<ArrayList<ResponsePvtMode>>(fileContent, type) ?: arrayListOf()
        positions.addAll(pos)

        initMap()
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
        positions.forEach {
            mMap?.addMarker(MarkerOptions().position(it.position))
        }
        if (positions.isNotEmpty()) {
            moveCameraWithZoom(positions[0].position)
        }

    }

    private fun moveCamera(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun moveCameraWithZoom(latLng: LatLng) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }

    companion object {
        const val POSITIONS_EXTRA: String = "positions"
    }
}
