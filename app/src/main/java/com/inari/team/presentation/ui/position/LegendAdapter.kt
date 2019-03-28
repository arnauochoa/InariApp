package com.inari.team.presentation.ui.position

import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.base.BaseAdapter
import com.inari.team.core.utils.extensions.context
import com.inari.team.core.utils.extensions.inflate
import com.inari.team.presentation.model.Mode
import kotlinx.android.synthetic.main.item_legend.view.*

class LegendAdapter : BaseAdapter<LegendAdapter.LegendViewHolder, Mode>() {

    override fun onBindViewHolder(holder: LegendViewHolder, item: Mode) {

        holder.color.setCardBackgroundColor(ContextCompat.getColor(context, item.color))
        holder.mode.text = item.name

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LegendViewHolder {
        return LegendViewHolder(inflate(R.layout.item_legend, p0))
    }


    class LegendViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val color: CardView = view.cvColor
        val mode: TextView = view.tvMode
    }
}