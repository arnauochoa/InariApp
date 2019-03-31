package com.inari.team.computation.utils


// TODO: redundant functions
fun applyMod(number: Double, modValue: Long): Double {
    return number.rem(modValue)
}

fun rad2deg(rad: Double): Double{
    return Math.toDegrees(rad)
}

fun deg2rad(deg: Double): Double{
    return Math.toRadians(deg)
}