package com.inari.team.core.utils

import com.inari.team.R
import com.inari.team.core.utils.extensions.context

object StringFormatter {

    fun formatDistanceNoDot(meters: Double): String = when {
        (meters < 1_000) ->
            context.getString(R.string.meters, meters)
        else ->
            context.getString(R.string.kilometers, (meters / 1_000))
    }


    fun formatPrice(price: Double): String = context.getString(R.string.euros, price)
}