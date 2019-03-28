package com.inari.team.presentation.ui.statistics


import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.skyplot.GnssEventsListener
import kotlinx.android.synthetic.main.fragment_statistics.*
import javax.inject.Inject


class StatisticsFragment : BaseFragment(), GnssEventsListener {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spGraphType.setOnItemClickListener { parent, v, position, id ->

        }
    }

    override fun onGnssStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGnssStopped() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGnssMeasurementsReceived(event: GnssMeasurementsEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOrientationChanged(orientation: Double, tilt: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNmeaMessageReceived(message: String?, timestamp: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationReceived(location: Location?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
