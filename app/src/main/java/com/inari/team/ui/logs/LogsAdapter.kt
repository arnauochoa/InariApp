package com.inari.team.ui.logs

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.inari.team.R
import kotlinx.android.synthetic.main.item_log.view.*
import java.io.File

class LogsAdapter(val context: Context) : RecyclerView.Adapter<LogsAdapter.LogsViewHolder>() {

    private val logs: ArrayList<File> = ArrayList()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
            LogsViewHolder(LayoutInflater.from(context).inflate(R.layout.item_log, p0, false))

    override fun getItemCount(): Int = logs.size

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {

        val item = logs[position]
        holder.name.text = item.name

        holder.layout.setOnClickListener {
            val i = Intent(context, LogsDetailActivity::class.java)
            i.putExtra("fileName", item.name)
            context.startActivity(i)
        }

    }

    fun setLogs(logs: Array<File>) {
        this.logs.clear()
        this.logs.addAll(logs)
        notifyDataSetChanged()
    }


    inner class LogsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.logName
        val layout: ConstraintLayout = itemView.layout
    }
}