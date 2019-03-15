package com.inari.team.ui.position

import com.google.android.gms.maps.model.LatLng

interface PositionView {
    fun onPositionCalculated(position: LatLng)
    fun showError(error: String)
    fun showMessage(message: String)
}