package com.inari.team.presentation.ui.logs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.BarAdapter
import kotlinx.android.synthetic.main.fragment_logs.*


class LogsFragment : BaseFragment() {

    private val positionLogsFragment = PositionLogsFragment()
    private val measurementLogsFragment = MeasurementsLogsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
    }

    private fun setViews() {
        val adapter = BarAdapter(childFragmentManager)

        adapter.addFragments(positionLogsFragment, "Position Logs")
        adapter.addFragments(measurementLogsFragment, "Measurements Logs")

        viewPager.adapter = adapter
        tabLayout.addTab(tabLayout.newTab().setText("Position Logs"))
        tabLayout.addTab(tabLayout.newTab().setText("Measurement Logs"))
        tabLayout.setupWithViewPager(viewPager)

    }

    fun updateFiles() {
        positionLogsFragment.setFiles()
        measurementLogsFragment.setFiles()
    }

}
