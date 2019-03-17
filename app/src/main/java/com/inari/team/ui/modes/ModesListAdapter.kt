package com.inari.team.ui.modes

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.data.Mode
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.toast

class ModesListAdapter(val context: Context) :
    RecyclerView.Adapter<ModesViewHolder>() {

    private val mPrefs = AppSharedPreferences.getInstance()
    private var modes = mPrefs.getModesList()

    private var expandedArray: BooleanArray = BooleanArray(modes.size)

    override fun getItemCount(): Int {
        return modes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModesViewHolder {
        return ModesViewHolder(LayoutInflater.from(context).inflate(R.layout.mode_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ModesViewHolder, position: Int) {
        val mode = modes[position]
        var expanded = expandedArray[position]

        holder.modeName.text = mode.name
        setDescription(holder, mode)
        holder.setExpanded(expanded)

        holder.itemView.setOnClickListener {
            expandedArray[position] = !expanded
            notifyItemChanged(position)
        }

        holder.deleteButton.setOnClickListener {
            toast("Mode '" + modes[position].name + "' deleted")
            modes = mPrefs.deleteMode(mode)
            this.notifyDataSetChanged()
        }

    }

    private fun setDescription(holder: ModesViewHolder, mode: Mode) {
        holder.constellationsDescription.text = mode.constellationsAsString()
        holder.bandsDescription.text = mode.bandsAsString()
        holder.correctionsDescription.text = mode.correctionsAsString()
        holder.algorithmDescription.text = mode.algorithmAsString()
    }

    fun update() {
        modes = mPrefs.getModesList()
        expandedArray = expandedArray.plus(false)
        this.notifyDataSetChanged()
    }
}