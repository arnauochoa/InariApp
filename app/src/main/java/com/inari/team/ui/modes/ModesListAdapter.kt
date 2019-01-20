package com.inari.team.ui.modes

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.data.Mode

class ModesListAdapter(private val modes: ArrayList<Mode>, val context: Context) :
    RecyclerView.Adapter<ModesViewHolder>() {

    private val expandedArray: BooleanArray = BooleanArray(modes.size)

    override fun getItemCount(): Int {
        return modes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModesViewHolder {
        return ModesViewHolder(LayoutInflater.from(context).inflate(R.layout.mode_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ModesViewHolder, position: Int) {
        val mode = modes[position]
        val expanded = expandedArray[position]

        holder.modeName.text = mode.name
        holder.constellationsDescription.text = mode.constellationsAsString()
        holder.bandsDescription.text = mode.bandsAsString()
        holder.setExpanded(expanded)

        holder.itemView.setOnClickListener {
            expandedArray[position] = !expanded
            notifyItemChanged(position)
        }


    }

    private fun getDescription(mode: Mode): CharSequence {
        return mode.constellations.toString()
    }
}