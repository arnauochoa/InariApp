package com.inari.team.ui.modes

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.inari.team.R
import kotlinx.android.synthetic.main.mode_list_item.view.*

class ModesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val v = view
    val modeName: TextView = view.itemTitle
    val constellationsDescription: TextView = view.constellationsDescription
    val bandsDescription: TextView = view.bandsDescription
    val correctionsDescription: TextView = view.correctionsDescription
    val algorithmDescription: TextView = view.algorithmDescription
    val deleteButton: ImageButton = view.deleteButton

    fun setExpanded(expanded: Boolean) {
        if (expanded) {
            v.itemContent.visibility = View.VISIBLE
            v.expandIndicator.setBackgroundResource(R.drawable.ic_arrow_up_grey)
        } else {
            v.itemContent.visibility = View.GONE
            v.expandIndicator.setBackgroundResource(R.drawable.ic_arrow_down_grey)
        }
    }

}