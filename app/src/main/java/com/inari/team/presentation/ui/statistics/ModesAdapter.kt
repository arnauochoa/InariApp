package com.inari.team.presentation.ui.statistics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.inari.team.R
import com.inari.team.presentation.model.Mode
import kotlinx.android.synthetic.main.item_mode.view.*

class ModesAdapter(val ctx: Context, val modes: ArrayList<Mode>, selectedModeId: Int, modeToRemoveId: Int) :
    ArrayAdapter<Mode>(ctx, R.layout.item_mode, modes) {

    val items = arrayListOf<Mode>()
    val firstTime = true

    var selectedMode: Mode?

    init {
        val modeToRemove = modes.find { mode -> mode.id == modeToRemoveId }
        selectedMode = modes.find { mode -> mode.id == selectedModeId }
        selectedMode?.let {
            modeToRemove?.let { remove ->
                updateList(it, remove)
            }
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        createItemView(position, parent)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        createItemView(position, parent)


    override fun getCount(): Int = items.size

    private fun createItemView(position: Int, parent: ViewGroup): View {

        val view = LayoutInflater.from(ctx).inflate(R.layout.item_mode, parent, false)

        val mode = items[position]

        view.modeName.text = mode.name

        return view
    }

    fun getItems(): List<Mode> = items

    fun updateList(selectedMode: Mode, modeToRemove: Mode) {
        this.items.clear()
        this.items.addAll(modes)
        this.items.remove(modeToRemove)
        this.items.remove(selectedMode)
        this.items.add(0, selectedMode)
        notifyDataSetChanged()
    }

}