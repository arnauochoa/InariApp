package com.inari.team.presentation.ui.tutorial

import android.view.View
import com.bumptech.glide.Glide
import com.inari.team.R
import com.inari.team.core.base.BasePagerAdapter
import com.inari.team.core.utils.context
import kotlinx.android.synthetic.main.item_tutorial_layout.view.*

class TutorialPagerAdapter : BasePagerAdapter<Int>(R.layout.item_tutorial_layout) {
    override fun bindItem(view: View, item: Int) {
        Glide.with(context).load(item).into(view.ivTutorial)
    }

}