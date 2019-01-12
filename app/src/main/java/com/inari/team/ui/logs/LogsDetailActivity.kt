package com.inari.team.ui.logs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.inari.team.R
import com.inari.team.utils.retrieveFile
import kotlinx.android.synthetic.main.activity_logs_detail.*

class LogsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs_detail)

        val fileName = intent.getStringExtra("fileName")
        if (fileName.isNotBlank()) {

            val fileText = retrieveFile(fileName)
            file.text = fileText
        }


    }
}
