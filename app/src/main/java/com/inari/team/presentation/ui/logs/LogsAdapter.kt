package com.inari.team.presentation.ui.logs

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.inari.team.R
import com.inari.team.core.utils.deleteFile
import com.inari.team.core.utils.toast
import kotlinx.android.synthetic.main.item_log.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class LogsAdapter(val context: Context) : RecyclerView.Adapter<LogsAdapter.LogsViewHolder>() {

    private val logs: ArrayList<File> = ArrayList()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        LogsViewHolder(LayoutInflater.from(context).inflate(R.layout.item_log, p0, false))

    override fun getItemCount(): Int = logs.size

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {

        val item = logs[position]

        val date = Date(item.lastModified())
        val simpleDate = SimpleDateFormat("dd/MM/yy hh:mm", Locale.ENGLISH).format(date)

        holder.name.text = item.name
        holder.detail.text = "$simpleDate  ${item.length()}kB"

        holder.layout.setOnClickListener {
            val i = Intent(context, LogsDetailActivity::class.java)
            i.putExtra("fileName", item.name)
            context.startActivity(i)
        }

        holder.layout.setOnLongClickListener {
            if (holder.share.visibility == VISIBLE) {
                holder.share.visibility = GONE
                holder.delete.visibility = VISIBLE

            } else {
                holder.share.visibility = VISIBLE
                holder.delete.visibility = GONE
            }
            true
        }

        holder.delete.setOnClickListener {
            if (deleteFile(item.name)) {
                logs.removeAt(position)
                notifyDataSetChanged()
            } else {
                toast("Error occurred deleting file, please delete it manually")
            }
        }

        holder.share.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Here is my log file.")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Send File..."))
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
        val share: ImageView = itemView.share
        val detail: TextView = itemView.tvDetails
        val delete: ImageView = itemView.ivDelete
    }
}