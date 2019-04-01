package com.inari.team.computation.utils

object Constants {

    const val C = 299792458
    const val OMEGA_EARTH_DOT = 7.2921151467e-5
    const val GM = 3.986008e14
    const val HALF_WEEK = 302400


    const val L1_FREQ = 157542000000.0
    const val L5_FREQ = 117645000000.0

    const val PVT_ITER = 5

    //Modes constants
    const val CN0_MASK = 1
    const val ELEVATION_MASK = 2

    const val GPS = 1
    const val GALILEO = 2

    const val GPS_KEY = "gps"
    const val GALILEO_E1_KEY = "galileo_E1"
    const val GALILEO_E5A_KEY = "galileo_E5a"

    const val L1 = 1
    const val L5 = 2
    const val E1 = 3
    const val E5A = 4

}