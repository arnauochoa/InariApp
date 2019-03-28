package com.inari.team.core.utils

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.ScatterChart
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.ui.statisticsdetail.StatisticsDetailActivity


const val BAND1_DOWN_THRES = 1575000000
const val BAND1_UP_THRES = 1576000000
const val BAND5_DOWN_THRES = 1176000000
const val BAND5_UP_THRES = 1177000000

const val L1_E1 = 1
const val L5_E5 = 2

class SatElevCNo(var svid: Int, var elevation: Float, var cNo: Float)

fun createScatterChart(
    context: Context?,
    xMin: Float,
    xMax: Float,
    yMin: Float,
    yMax: Float
): ScatterChart? {
    val scatterChart = ScatterChart(context)

    scatterChart?.let {
        it.xAxis.axisMinimum = xMin
        it.xAxis.axisMaximum = xMax
        it.axisLeft.axisMinimum = yMin
        it.axisLeft.axisMaximum = yMax
        it.axisRight.isEnabled = false


        val chartLP = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        it.layoutParams = chartLP
    }

    return scatterChart
}

fun obtainCnoElevValues(selectedBand: Int, status: GnssStatus): ArrayList<SatElevCNo> {
    val satElevCNoList = arrayListOf<SatElevCNo>()
    with(status) {
        for (sat in 0 until satelliteCount) {
            if (isSelectedBand(selectedBand, getCarrierFrequencyHz(sat))) { // Add it if it is in selected band
                // Obtain point information
                satElevCNoList.add(
                    SatElevCNo(
                        getSvid(sat),
                        getElevationDegrees(sat),
                        getCn0DbHz(sat)
                    )
                )
            } // If not in selected frequency, do nothing
        } // End for
    }
    return satElevCNoList
}

fun isSelectedBand(selectedBand: Int, carrierFrequencyHz: Float): Boolean {
    var isSelected = false
    when (selectedBand) {
        L1_E1 -> {
            if (carrierFrequencyHz > BAND1_DOWN_THRES &&  // If carrierFrequencyHz inside L1_E1 band
                carrierFrequencyHz < BAND1_UP_THRES
            ) {
                isSelected = true
            }
        }
        L5_E5 ->{
            if (carrierFrequencyHz > BAND5_DOWN_THRES &&   // If carrierFrequencyHz inside L5_E5 band
                carrierFrequencyHz < BAND5_UP_THRES){
                isSelected = true
            }
        }
    }
    return isSelected
}