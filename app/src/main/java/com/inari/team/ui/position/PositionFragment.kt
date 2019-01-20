package com.inari.team.ui.position

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
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
import com.inari.team.R
import com.inari.team.ui.logs.LogsActivity
import com.inari.team.utils.saveFile
import com.inari.team.utils.toast
import kotlinx.android.synthetic.main.dialog_save_log.view.*
import kotlinx.android.synthetic.main.fragment_position.*
import kotlinx.android.synthetic.main.view_bottom_sheet.*
import okhttp3.MediaType
import okhttp3.ResponseBody


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
            if (clBottomSheet.visibility == View.VISIBLE) {
                clBottomSheet.visibility = View.GONE
            }
        }

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
                    var format = ".rinex"
                    if (layout.radioGroupFormat.checkedRadioButtonId != R.id.rinex) {
                        format = ".nma"
                    }
                    saveLog(fileName + format)
                    dialog.dismiss()
                } else toast("empty filename")
            }
            dialog.show()
        }
    }

    private fun saveLog(fileName: String) {
        saveFile(fileName, ResponseBody.create(MediaType.parse("text/plain"), "abcscnvoiernavodsnvo"))
        showSavedSnackBar()
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

    private fun showSavedSnackBar() {
        val snackbar = Snackbar.make(snackbarCl, "File saved", Snackbar.LENGTH_LONG)
        snackbar.setAction("OPEN") {
            context?.let {
                startActivity(Intent(it, LogsActivity::class.java))
            }
        }

        snackbar.show()
    }

}
