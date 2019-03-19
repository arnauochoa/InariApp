package com.inari.team.presentation.ui.status

import android.arch.lifecycle.MutableLiveData
import com.inari.team.core.base.BaseViewModel
import com.inari.team.core.utils.extensions.Data
import com.inari.team.core.utils.extensions.updateData
import com.inari.team.core.utils.getStatusData
import com.inari.team.data.GnssStatus
import com.inari.team.presentation.model.StatusData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusViewModel @Inject constructor() : BaseViewModel() {

    var status = MutableLiveData<Data<StatusData>>()

    fun obtainStatusParameters(gnssStatus: GnssStatus, selectedConstellation: StatusFragment.Companion.CONSTELLATION) {
        status.updateData(getStatusData(gnssStatus, selectedConstellation))
    }

}