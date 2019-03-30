package com.inari.team.presentation.ui.maplog

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
import com.inari.team.core.utils.getLegendColor
import kotlinx.android.synthetic.main.item_legend.view.*

class MapLogLegendAdapter :
    BaseAdapter<MapLogLegendAdapter.MapLogLegendViewHolder, MapLogLegendAdapter.MapLogLegendItem>() {

    override fun onBindViewHolder(holder: MapLogLegendViewHolder, item: MapLogLegendItem) {
        holder.color.setCardBackgroundColor(ContextCompat.getColor(context, getLegendColor(item.color)))
        holder.mode.text = item.name
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MapLogLegendViewHolder {
        return MapLogLegendViewHolder(inflate(R.layout.item_legend, p0))
    }


    class MapLogLegendViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val color: CardView = view.cvColor
        val mode: TextView = view.tvMode
    }

    class MapLogLegendItem(
        val color: Int,
        val name: String
    )
}