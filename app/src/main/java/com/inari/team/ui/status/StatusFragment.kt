package com.inari.team.ui.status


import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.PendingResult
import com.inari.team.R
import com.inari.team.ui.status.all_status.AllStatusFragment
import com.inari.team.ui.status.galileo_status.GalileoStatusFragment
import com.inari.team.ui.status.gps_status.GPSStatusFragment
import com.inari.team.utils.BarAdapter
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    private var gpsStatusFragment = GPSStatusFragment()
    private var galileoStatusFragment = GalileoStatusFragment()
    private var allStatusFragment = AllStatusFragment()

    private var mListener: PendingResult.StatusListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewPager()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mListener = context as? PendingResult.StatusListener
    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(childFragmentManager)

        tabLayout.addTab(tabLayout.newTab().setText("GPS"))
        tabLayout.addTab(tabLayout.newTab().setText("Galileo"))
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        pagerAdapter.addFragments(gpsStatusFragment, "GPS")
        pagerAdapter.addFragments(galileoStatusFragment, "Galileo")
        pagerAdapter.addFragments(allStatusFragment, "All")

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 0

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }

        })

    }

}
