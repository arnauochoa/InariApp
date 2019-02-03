package com.inari.team.ui.status


import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.ui.status.all_status.AllStatusFragment
import com.inari.team.ui.status.galileo_status.GalileoStatusFragment
import com.inari.team.ui.status.gps_status.GPSStatusFragment
import com.inari.team.utils.BarAdapter
import kotlinx.android.synthetic.main.fragment_status.*

class StatusFragment : Fragment() {

    companion object {
        const val FRAG_TAG = "status_fragment"
    }

    private var gpsStatusFragment: GPSStatusFragment? = null
    private var galileoStatusFragment: GalileoStatusFragment? = null
    private var allStatusFragment: AllStatusFragment? = null

    private var mListener: StatusListener? = null

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
        mListener = context as? StatusListener
    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(childFragmentManager)

        tabLayout.addTab(tabLayout.newTab().setText("GPS"))
        tabLayout.addTab(tabLayout.newTab().setText("Galileo"))
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        prepareFragments()

        pagerAdapter.addFragments(gpsStatusFragment, "GPS")
        pagerAdapter.addFragments(galileoStatusFragment, "Galileo")
        pagerAdapter.addFragments(allStatusFragment, "All")

        //viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 2
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

    private fun prepareFragments(){
        gpsStatusFragment = GPSStatusFragment()
        galileoStatusFragment = GalileoStatusFragment()
        allStatusFragment = AllStatusFragment()

        gpsStatusFragment?.let {
            mListener?.onGpsStatusFragmentSet(it)
        }
        galileoStatusFragment?.let {
            mListener?.onGalileoStatusFragmentSet(it)
        }
        allStatusFragment?.let {
            mListener?.onAllStatusFragmentSet(it)
        }
    }

    interface StatusListener{
        fun onGpsStatusFragmentSet(gpsStatusFragment: GPSStatusFragment)
        fun onGalileoStatusFragmentSet(galileoStatusFragment: GalileoStatusFragment)
        fun onAllStatusFragmentSet(allStatusFragment: AllStatusFragment)
    }

}
