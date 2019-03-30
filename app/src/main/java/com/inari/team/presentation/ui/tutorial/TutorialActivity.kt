package com.inari.team.presentation.ui.tutorial

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View.INVISIBLE
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
        tlSlider.setupWithViewPager(viewPager)

        val tutorialSteps = arrayListOf(
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position,
            R.drawable.tutorial_screen_position
        )

        val titles = arrayListOf(
            getString(R.string.position_bottom),
            getString(R.string.gnss_state_bottom),
            getString(R.string.statistics_bottom),
            getString(R.string.logs_bottom),
            getString(R.string.about_bottom)
        )

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                if (p0 == tutorialSteps.size - 1) {
                    tvForward.text = getString(R.string.finish)
                    tvSkip.visibility = INVISIBLE
                }
                tvTitle.text = titles[p0]
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
