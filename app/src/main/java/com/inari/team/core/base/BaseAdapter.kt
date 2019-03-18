package com.inari.team.core.base

import android.support.v7.widget.RecyclerView

abstract class BaseAdapter<K : RecyclerView.ViewHolder, L> : RecyclerView.Adapter<K>() {

    protected val mItems: ArrayList<L> = arrayListOf()
    protected var mFilteredItems: ArrayList<L> = arrayListOf()

    override fun onBindViewHolder(p0: K, p1: Int) {
        onBindViewHolder(p0, mFilteredItems[p1])
    }

    abstract fun onBindViewHolder(holder: K, item: L)

    override fun getItemCount(): Int = mFilteredItems.size

    fun setItems(items: List<L>) {
        mItems.clear()
        mFilteredItems.clear()
        mItems.addAll(items)
        mFilteredItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<L>) {
        mItems.addAll(items)
        mFilteredItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: L) {
        mItems.add(item)
        mFilteredItems.add(item)
        notifyDataSetChanged()
    }

    fun clear() {
        mItems.clear()
        mFilteredItems.clear()
        notifyDataSetChanged()
    }

}