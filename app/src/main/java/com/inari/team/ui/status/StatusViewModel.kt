package com.inari.team.ui.status

import android.arch.lifecycle.MutableLiveData
import android.location.GnssStatus
import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.getCNoString
import com.inari.team.core.utils.getSatellitesCount
import com.inari.team.core.utils.obtainCNos
import com.inari.team.data.StatusData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusViewModel @Inject constructor() : BaseViewModel() {

    var status = MutableLiveData<Data<StatusData>>()

    fun obtainStatusParameters(gnssStatus: GnssStatus) {
        val statusData = StatusData()

        val allCNos = obtainCNos(gnssStatus)

        statusData.CN0 = getCNoString(allCNos)
        statusData.satellitesCount = getSatellitesCount(allCNos)

        status.updateData(statusData)
    }

}