package com.inari.team.core.utils.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.inari.team.core.utils.isNetworkAvailable

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isNetworkAvailable())
        }

    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}