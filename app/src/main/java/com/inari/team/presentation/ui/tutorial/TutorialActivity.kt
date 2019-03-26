package com.inari.team.presentation.ui.tutorial

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
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

        val tutorialSteps = arrayListOf(
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position
        )

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                if (p0 == tutorialSteps.size - 1) {
                    tvForward.text = "finish"
                    tvSkip.visibility = GONE
                }
            }

            override fun onPageSelected(p0: Int) {
            }

        })

        tutorialPagerAdapter.addItems(tutorialSteps)

        tvSkip.setOnClickListener {
            finish()
        }

        tvForward.setOnClickListener {
            if (tvForward.text == "finish") {
                finish()
            } else {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }


    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

}
