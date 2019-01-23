package com.inari.team.ui.logs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.inari.team.R
import com.inari.team.utils.getFilesList
import com.inari.team.utils.toast
import kotlinx.android.synthetic.main.activity_logs.*

class LogsActivity : AppCompatActivity() {

    private var adapter: LogsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var filesList = getFilesList()

        if (filesList.isEmpty()){
            toast("There are no files yet")
        }

        adapter = LogsAdapter(this)
        rvLogs.layoutManager = LinearLayoutManager(this)
        rvLogs.adapter = adapter

        adapter?.setLogs(getFilesList())

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == android.R.id.home){
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
