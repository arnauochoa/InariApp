package com.inari.team.ui.modes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.inari.team.R
import com.inari.team.data.Mode
import kotlinx.android.synthetic.main.item_modes_child.view.*
import kotlinx.android.synthetic.main.item_modes_parent.view.*

class ExpandableModesListAdapter(
    val context: Context, listDataHeader: ArrayList<Mode>, private val listChild: List<Mode> // header titles
    // child data in format of header title, child title
) : BaseExpandableListAdapter() {

    private var listDataHeaderFiltered: ArrayList<Mode> = listDataHeader
    private var listDataHeaderOriginal = ArrayList<Mode>()

    init {
        listDataHeaderOriginal.addAll(listDataHeader)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listDataHeaderFiltered[groupPosition].constellations.toString()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        var convView = convertView

        val childText = getChild(groupPosition, childPosition) as String

        if (convView == null) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convView = inflater.inflate(R.layout.item_modes_child, null)
        }

        convView!!.modeDescription.text = childText

        return convView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return this.listDataHeaderFiltered[groupPosition].name
    }

    override fun getGroupCount(): Int {
        return this.listDataHeaderFiltered.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        var convView = convertView
        val headerTitle = getGroup(groupPosition) as String
        if (convView == null) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convView = inflater.inflate(R.layout.item_modes_parent, null)
        }

//        if(groupPosition % 2 == 1) {
//            convertView?.setBackgroundResource(R.color.colorLightGrayBackground)
//        } else {
//            convertView?.setBackgroundResource(R.color.colorDarkGrayBackground)
//        }

        convView!!.modeTitle.text = headerTitle

        return convView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}