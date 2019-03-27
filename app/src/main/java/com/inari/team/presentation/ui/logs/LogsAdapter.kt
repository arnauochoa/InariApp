package com.inari.team.presentation.ui.logs

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.content.FileProvider
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
import com.inari.team.core.utils.getFile
import com.inari.team.core.utils.showAlert
import kotlinx.android.synthetic.main.item_log.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogsAdapter(val context: Context, private val emptyViewAction: () -> Unit) :
    RecyclerView.Adapter<LogsAdapter.LogsViewHolder>() {

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
            val file = getFile(item.name)
            openFile(file.absolutePath, context)
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
                holder.delete.visibility = GONE
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

        holder.share.setOnClickListener {
//            val sendIntent: Intent = Intent().apply {
//                action = Intent.ACTION_SEND
//                putExtra(Intent.EXTRA_TEXT, "Here is my log file.")
//                type = "text/plain"
//            }
//            context.startActivity(Intent.createChooser(sendIntent, "Send File..."))
            val file = getFile(item.name)
            shareFile(file.absolutePath)
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

    private fun openFile(filename: String, context: Context) {
        val data = FileProvider.getUriForFile(context, "com.inari.team.provider", File(filename))
        context.grantUriPermission(context.packageName, data, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, "text/plain")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun shareFile(filename: String){
        val data = FileProvider.getUriForFile(context, "com.inari.team.provider", File(filename))
        context.grantUriPermission(context.packageName, data, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_STREAM, data)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share log"))
    }
}