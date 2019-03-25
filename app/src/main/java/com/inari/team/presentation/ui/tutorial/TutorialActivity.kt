package com.inari.team.presentation.ui.tutorial

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.core.utils.extensions.enableFullScreen
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        enableFullScreen(window)

        setViewPager()
    }

    private fun setViewPager() {
        val tutorialPagerAdapter = TutorialPagerAdapter()
        viewPager.adapter = tutorialPagerAdapter
        tlSlider.attachToViewPager(viewPager)



        tutorialPagerAdapter.addItems(
            arrayListOf(
                R.drawable.tutorial_screen_position,
                R.drawable.tutorial_screen_position,
                R.drawable.tutorial_screen_position,
                R.drawable.tutorial_screen_position,
                R.drawable.tutorial_screen_position
            )
        )

    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

}
