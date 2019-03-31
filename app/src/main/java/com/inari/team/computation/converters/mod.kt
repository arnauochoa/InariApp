package com.inari.team.computation.converters

fun applyMod(number: Double, modValue: Long): Double {
    return number.rem(modValue)
}