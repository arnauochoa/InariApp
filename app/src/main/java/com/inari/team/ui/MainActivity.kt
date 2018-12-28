package com.inari.team.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.ui.position.PositionFragment
import com.inari.team.ui.statistics.StatisticsFragment
import com.inari.team.ui.status.StatusFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(PositionFragment(), PositionFragment.FRAG_TAG)

        setBottomNavigation()
    }

    private fun setBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_stations -> {
                    replaceFragment(PositionFragment(), PositionFragment.FRAG_TAG)
                }
                R.id.action_maps -> {
                    replaceFragment(StatusFragment(), StatusFragment.FRAG_TAG)
                }
                else -> {
                    replaceFragment(StatisticsFragment(), StatisticsFragment.FRAG_TAG)
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager?.beginTransaction()
                ?.addToBackStack(tag)
                ?.replace(R.id.container, fragment)
                ?.commit()
    }
}
