package com.inari.team.ui.logs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.inari.team.R
import com.inari.team.utils.retrieveFile
import kotlinx.android.synthetic.main.activity_logs_detail.*

class LogsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)


        val fileName = intent.getStringExtra("fileName")
        if (fileName.isNotBlank()) {
            supportActionBar?.title = fileName

            val fileText = retrieveFile(fileName)
            file.text = fileText
        }
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
