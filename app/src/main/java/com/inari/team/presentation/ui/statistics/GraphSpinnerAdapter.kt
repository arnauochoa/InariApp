package com.inari.team.presentation.ui.statistics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.inari.team.R
import kotlinx.android.synthetic.main.item_graph.view.*

class GraphSpinnerAdapter(val ctx: Context, val modes: ArrayList<String>) :
    ArrayAdapter<String>(ctx, R.layout.item_mode, modes) {

    var items = arrayListOf<String>()

    init {
        items = modes
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        createItemView(position, parent)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        createItemView(position, parent)


    override fun getCount(): Int = items.size

    private fun createItemView(position: Int, parent: ViewGroup): View {

        val view = LayoutInflater.from(ctx).inflate(R.layout.item_graph, parent, false)

        val graph = items[position]

        view.tvGraphType.text = graph

        return view
    }

    fun getItems(): List<String> = items

}