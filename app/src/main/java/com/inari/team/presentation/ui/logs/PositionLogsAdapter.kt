package com.inari.team.presentation.ui.logs

import android.content.Context
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
import com.inari.team.core.utils.showAlert
import kotlinx.android.synthetic.main.item_log.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PositionLogsAdapter(
    val context: Context, private val emptyViewAction: () -> Unit,
    private val clickAction: (String) -> Unit
) :
    RecyclerView.Adapter<PositionLogsAdapter.PositionLogsViewHolder>() {

    private val logs: ArrayList<File> = ArrayList()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        PositionLogsViewHolder(LayoutInflater.from(context).inflate(R.layout.item_log, p0, false))

    override fun getItemCount(): Int = logs.size

    override fun onBindViewHolder(holder: PositionLogsViewHolder, position: Int) {

        val item = logs[position]

        val date = Date(item.lastModified())
        val simpleDate = SimpleDateFormat("dd/MM/yy hh:mm", Locale.ENGLISH).format(date)

        holder.name.text = item.name
        holder.detail.text = simpleDate

        holder.layout.setOnClickListener {
            if (holder.delete.visibility == VISIBLE) {
                holder.delete.visibility = GONE
            } else {
                clickAction.invoke(item.name)
            }
        }

        holder.layout.setOnLongClickListener {
            if (holder.share.visibility == View.VISIBLE) {
                holder.share.visibility = View.GONE
                holder.delete.visibility = View.VISIBLE

            } else {
                holder.share.visibility = View.VISIBLE
                holder.delete.visibility = View.GONE
            }
            true
        }

        holder.delete.setOnClickListener {
            if (deleteFile(item.name)) {
                holder.delete.visibility = View.GONE
                holder.share.visibility = View.VISIBLE
                logs.removeAt(position)
                notifyDataSetChanged()
                if (logs.size == 0) {
                    emptyViewAction.invoke()
                }
            } else {
                showAlert(
                    context, "Error",
                    "Error deleting file, please delete it manually in Internal Storage", "", {},
                    true
                )
            }
        }

        holder.share.visibility = View.GONE

    }

    fun setLogs(logs: Array<File>) {
        this.logs.clear()
        this.logs.addAll(logs)
        notifyDataSetChanged()
    }


    inner class PositionLogsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.logName
        val layout: ConstraintLayout = itemView.layout
        val share: ImageView = itemView.share
        val detail: TextView = itemView.tvDetails
        val delete: ImageView = itemView.ivDelete
    }

}