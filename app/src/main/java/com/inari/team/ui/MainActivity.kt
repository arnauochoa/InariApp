package com.inari.team.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.ui.position.PositionFragment
import com.inari.team.ui.statistics.StatisticsFragment
import com.inari.team.ui.status.StatusFragment
import com.inari.team.utils.BarAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setViewPager()
        setBottomNavigation()
    }

    private fun setViewPager() {
        val pagerAdapter = BarAdapter(supportFragmentManager)

        pagerAdapter.addFragments(PositionFragment(), "Position")
        pagerAdapter.addFragments(StatusFragment(), "GNSS state")
        pagerAdapter.addFragments(StatisticsFragment(), "Statistics")

        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = 0

    }

    private fun setBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_position -> {
                    switchFragment(0)
                }
                R.id.action_status -> {
                    switchFragment(1)
                }
                R.id.action_statistics -> {
                    switchFragment(2)
                }
            }
            true
        }
    }

    private fun switchFragment(id: Int) {
        viewPager.setCurrentItem(id, false)
    }

}
