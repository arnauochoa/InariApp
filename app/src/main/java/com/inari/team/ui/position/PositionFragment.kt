package com.inari.team.ui.position

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
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
import com.inari.team.R
import kotlinx.android.synthetic.main.fragment_position.*


class PositionFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val FRAG_TAG = "position_fragment"
    }

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mZoom = 0.30
    private var mapFragment: SupportMapFragment? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)
        setHasOptionsMenu(true)

        fabOptions.setOnClickListener {
            clBottomSheet.visibility = View.VISIBLE
        }

        fabClose.setOnClickListener {
            if (clBottomSheet.visibility == View.VISIBLE){
                clBottomSheet.visibility = View.GONE
            }
        }

    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            initMap()
        },1000)
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_log -> {
            }
            R.id.see_log -> {
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
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

}
