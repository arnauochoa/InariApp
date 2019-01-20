package com.inari.team.ui.logs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.inari.team.R
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

        holder.name.text = "${item.name}  $simpleDate  ${item.length()}kB"

        holder.layout.setOnClickListener {
            val i = Intent(context, LogsDetailActivity::class.java)
            i.putExtra("fileName", item.name)
            context.startActivity(i)
        }

        holder.share.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "abc@mail.com", null))
            emailIntent.putExtra(Intent.EXTRA_EMAIL, "address")
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body")
            context.startActivity(Intent.createChooser(emailIntent, "Send Email..."))

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
    }
}