package com.inari.team

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inari.team.computation.applyMask
import com.inari.team.computation.data.AcqInformation
import com.inari.team.computation.data.ResponsePvtMultiConst
import com.inari.team.computation.pvtMultiConst
import com.inari.team.computation.utils.Constants
import com.inari.team.presentation.model.ResponsePvtMode
import org.junit.Test

/**
 * Testing of the Pvt Algorithm
 */
class PvtUnitTest {

    @Test
    fun pvt_isCorrect() {

        //list of files that we want to compute
        val filesList = arrayListOf("galileo/E1/GAL_E1_1.txt")

        //for each file added in the list, compute the position
        filesList.forEach { fileName ->

            //Getting the file
            val fileInput = this.javaClass.classLoader?.getResourceAsStream(fileName)
            val acqInfoString = fileInput?.bufferedReader().use { it?.readText() }
            val type = object : TypeToken<AcqInformation>() {}.type

            //parse data into the object AcqInformation
            val acqInfo = Gson().fromJson<AcqInformation>(acqInfoString, type)

            //print the reference position from google in order to know the approximate position tha we should be getting
            print(
                "\n====================================================\n" +
                        "Reference Position: ${acqInfo.refLocation.refLocationLla.latitude}," +
                        " ${acqInfo.refLocation.refLocationLla.longitude}\n"
            )

            //start the computation
            val computedPositions = testComputePvt(acqInfo)

            //print the results obtained or error
            if (computedPositions.isNotEmpty()) {
                computedPositions.forEach {
                    print("${it.modeName} --> Computed Position:${it.compPosition}")
                }
            } else {
                print("Could not compute any positions")
            }

        }


    }

    private fun testComputePvt(acq: AcqInformation): List<ResponsePvtMode> {

        //initialize an empty array of responses, we should get one response of every selected mode
        val responses = arrayListOf<ResponsePvtMode>()

        var acqInformation = acq

        //CN0 mask
        if (acqInformation.cn0mask != 0) {
            acqInformation = applyMask(acqInformation, Constants.CN0_MASK)
        }
        //Elevation mask
        if (acqInformation.elevationMask != 0) {
            acqInformation = applyMask(acqInformation, Constants.ELEVATION_MASK)
        }

        //for each mode selected
        acqInformation.modes.forEach {

            //obtain pvt
            val pvtMultiConst = try {
                pvtMultiConst(acqInformation, it)
            } catch (e: Exception) {
                ResponsePvtMultiConst()
            }

            //if pvt is valid add it to responses list
            if (pvtMultiConst.pvt.lat in -180.0..180.0 && pvtMultiConst.pvt.lng in -180.0..180.0) {

                val pvtResponse = ResponsePvtMode(
                    LatLng(
                        acqInformation.refLocation.refLocationLla.latitude,
                        acqInformation.refLocation.refLocationLla.longitude
                    ),
                    acqInformation.refLocation.refLocationLla.altitude.toFloat(),
                    LatLng(pvtMultiConst.pvt.lat, pvtMultiConst.pvt.lng),
                    it.color,
                    it.name
                )

                responses.add(pvtResponse)
            }
        }

        return responses
    }
}
