package com.inari.team.presentation.ui.modes

import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.utils.AppSharedPreferences
import com.inari.team.core.utils.context
import com.inari.team.core.utils.toast
import com.inari.team.presentation.model.Mode

class ModesListAdapter : RecyclerView.Adapter<ModesViewHolder>() {

    private val mPrefs = AppSharedPreferences.getInstance()
    private var modes = mPrefs.getModesList()

    override fun getItemCount(): Int {
        return modes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModesViewHolder {
        return ModesViewHolder(LayoutInflater.from(context).inflate(R.layout.mode_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ModesViewHolder, position: Int) {
        val mode = modes[position]

        holder.modeName.text = mode.name
        setDescription(holder, mode)
        setSelected(holder, mode.isSelected)

        holder.itemView.setOnLongClickListener {
            holder.deleteButton.visibility = VISIBLE
            true
        }

        holder.itemView.setOnClickListener {
            if (holder.deleteButton.visibility == VISIBLE) {
                holder.deleteButton.visibility = GONE
            } else {
                mode.isSelected = !mode.isSelected
                setSelected(holder, mode.isSelected)
            }
        }

        holder.deleteButton.setOnClickListener {
            toast("Mode '" + modes[position].name + "' deleted")
            modes = mPrefs.deleteMode(mode)
            this.notifyDataSetChanged()
        }

    }

    private fun setSelected(holder: ModesViewHolder, selected: Boolean) {
        if (selected) {
            holder.modeName.setTextColor(getColor(context, R.color.black))
            holder.cvMode.elevation = 16f
            holder.cvMode.setCardBackgroundColor(getColor(context, R.color.colorAccentLight))
            holder.checkImage.visibility = VISIBLE
        } else {
            holder.modeName.setTextColor(getColor(context, R.color.gray))
            holder.cvMode.elevation = 0f
            holder.cvMode.setCardBackgroundColor(getColor(context, R.color.white))
            holder.checkImage.visibility = GONE
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
        this.notifyDataSetChanged()
    }

    fun getItems(): List<Mode> {
        return modes
    }
}