package com.inari.team.computation.utils

import com.inari.team.computation.Constants.C

fun checkTowDecode(tow: Int): Boolean {


    return true
}

fun checkTowKnown(tow: Int): Boolean {

    return true
}

fun checkGalState(tow: Int): Boolean {

    return true
}

fun getTtx(timeOffsetNanos: Double, receivedSvTimeNanos: Long): Double {
    return receivedSvTimeNanos + timeOffsetNanos
}

fun getPseudoRange(tTx: Double, tRx: Double): Double {
    return ((tRx - tTx) / 1000000000L) * C
}