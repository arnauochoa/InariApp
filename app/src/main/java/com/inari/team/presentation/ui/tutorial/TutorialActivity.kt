package com.inari.team.presentation.ui.tutorial

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        setViewPager()
    }

    private fun setViewPager(){
        val tutorialPagerAdapter = TutorialPagerAdapter()
        viewPager.adapter = tutorialPagerAdapter
        tlSlider.attachToViewPager(viewPager)
    }
}
