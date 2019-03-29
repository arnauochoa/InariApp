package com.inari.team.presentation.ui.logs


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.getFilesList
import kotlinx.android.synthetic.main.fragment_measurements_logs.*

class MeasurementsLogsFragment : BaseFragment() {

    private var adapter: LogsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_measurements_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)
    }

    private fun setViews(view: View) {
        adapter = LogsAdapter(view.context) {
            setFiles()
        }
        rvLogs.layoutManager = LinearLayoutManager(view.context)
        rvLogs.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            setFiles()
        }

        setFiles()
    }

    private fun setFiles() {
        val filesList = getFilesList()

        adapter?.setLogs(filesList)
        if (filesList.isEmpty()) {
            layoutEmptyView.visibility = View.VISIBLE
        } else {
            layoutEmptyView.visibility = GONE
        }

        swipeRefresh.isRefreshing = false
    }

}
