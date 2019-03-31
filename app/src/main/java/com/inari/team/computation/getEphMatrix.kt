package com.inari.team.computation

import com.inari.team.computation.utils.Constants.E1
import com.inari.team.computation.utils.Constants.E5A
import com.inari.team.computation.utils.Constants.GALILEO
import com.inari.team.computation.utils.Constants.GALILEO_E1_KEY
import com.inari.team.computation.utils.Constants.GALILEO_E5A_KEY
import com.inari.team.computation.utils.Constants.GPS
import com.inari.team.computation.data.AcqInformation
import com.inari.team.core.utils.GPS_KEY
import com.inari.team.presentation.model.Mode

fun getEphMatrix(acqInformation: AcqInformation, mode: Mode): HashMap<String, Any> {


    val ephMatrix = hashMapOf<String, Any>()

    mode.constellations.forEach {
        when (it) {
            GPS -> {
                //todo Arnau generate matrix or whatever
                ephMatrix[GPS_KEY] = Any()
            }
            GALILEO -> {
                mode.bands.forEach { band ->
                    when (band) {
                        E1 -> {
                            //todo Arnau generate matrix or whatever
                            ephMatrix[GALILEO_E1_KEY] = Any()
                        }
                        E5A -> {
                            //todo Arnau generate matrix or whatever
                            ephMatrix[GALILEO_E5A_KEY] = Any()
                        }
                    }
                }
            }
        }


    }

    return ephMatrix

}