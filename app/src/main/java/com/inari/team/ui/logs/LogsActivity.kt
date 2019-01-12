package com.inari.team.ui.logs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.inari.team.R
import com.inari.team.utils.getFilesList
import kotlinx.android.synthetic.main.activity_logs.*

class LogsActivity : AppCompatActivity() {

    private var adapter: LogsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        adapter = LogsAdapter(this)
        rvLogs.layoutManager = LinearLayoutManager(this)
        rvLogs.adapter = adapter

        adapter?.setLogs(getFilesList())

    }

}
