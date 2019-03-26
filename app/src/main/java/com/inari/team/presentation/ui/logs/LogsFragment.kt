package com.inari.team.presentation.ui.logs


import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.getFilesList
import kotlinx.android.synthetic.main.fragment_logs.*


class LogsFragment : BaseFragment() {

    private var adapter: LogsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(view.context)
        val dividerItemDecoration = DividerItemDecoration(rvLogs.context, RecyclerView.VERTICAL)

        adapter = LogsAdapter(view.context) {
            layoutEmptyView.visibility = VISIBLE
            rvLogs.visibility = GONE
        }
        rvLogs.layoutManager = layoutManager
        rvLogs.addItemDecoration(dividerItemDecoration)
        rvLogs.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            setFiles()
        }

        setFiles()
    }

    fun setFiles() {
        val filesList = getFilesList()

        if (filesList.isEmpty()) {
            rvLogs.visibility = GONE
            layoutEmptyView.visibility = VISIBLE
        } else {
            rvLogs.visibility = VISIBLE
            layoutEmptyView.visibility = GONE
            adapter?.setLogs(filesList)
        }

        swipeRefresh.isRefreshing = false
    }


}
