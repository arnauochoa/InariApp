package com.inari.team.presentation.model

import com.inari.team.presentation.model.PositionParameters.ALG_LS
import com.inari.team.presentation.model.PositionParameters.ALG_WLS
import com.inari.team.presentation.model.PositionParameters.BAND_L1
import com.inari.team.presentation.model.PositionParameters.BAND_L5
import com.inari.team.presentation.model.PositionParameters.CONST_GAL
import com.inari.team.presentation.model.PositionParameters.CONST_GPS
import com.inari.team.presentation.model.PositionParameters.CORR_IONOFREE
import com.inari.team.presentation.model.PositionParameters.CORR_IONOSPHERE
import com.inari.team.presentation.model.PositionParameters.CORR_TROPOSPHERE

data class Mode(
    var id: Int,
    val name: String = "",
    val constellations: ArrayList<Int>,
    val bands: ArrayList<Int>,
    val corrections: ArrayList<Int>,
    val algorithm: Int,
    var isSelected: Boolean,
    var color: Int = -1
) {
    /**
     * STRING GETTERS
     */

    fun constellationsAsString(): CharSequence? {
        var constellationsString = ""

        if (constellations.contains(CONST_GPS)) {
            constellationsString = "GPS"
        }
        if (constellations.contains(CONST_GAL)) {
            if (constellationsString.isNotBlank()) constellationsString += ", "
            constellationsString += "Galileo"
        }
        return constellationsString
    }

    fun bandsAsString(): CharSequence? {
        var bandsString = ""

        if (bands.contains(BAND_L1)) {
            bandsString = "L1"
        }
        if (bands.contains(BAND_L5)) {
            if (bandsString.isNotBlank()) bandsString += ", "
            bandsString += "L5"
        }
        return bandsString
    }

    fun correctionsAsString(): CharSequence? {
        var correctionsString = ""

        if (corrections.contains(CORR_IONOSPHERE)) {
            correctionsString = "Ionosphere"
        }
        if (corrections.contains(CORR_TROPOSPHERE)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Troposphere"
        }
        if (corrections.contains(CORR_IONOFREE)) {
            if (correctionsString.isNotBlank()) correctionsString += ", "
            correctionsString += "Iono-free"
        }
        if (correctionsString.isBlank()) correctionsString = "None"
        return correctionsString
    }

    fun algorithmAsString(): CharSequence? {
        var algorithmString = ""

        when (algorithm) {
            ALG_LS -> algorithmString = "Least Squares"
            ALG_WLS -> algorithmString = "Weighted Least Squares"
        }
        return algorithmString
    }

}
