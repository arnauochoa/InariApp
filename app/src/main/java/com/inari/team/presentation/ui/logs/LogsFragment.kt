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

        adapter.addFragments(PositionLogsFragment(), "Position Logs")
        adapter.addFragments(MeasurementsLogsFragment(), "Measurements Logs")

        viewPager.adapter = adapter
        tabLayout.addTab(tabLayout.newTab().setText("Position Logs"))
        tabLayout.addTab(tabLayout.newTab().setText("Measurement Logs"))
        tabLayout.setupWithViewPager(viewPager)

    }

}
