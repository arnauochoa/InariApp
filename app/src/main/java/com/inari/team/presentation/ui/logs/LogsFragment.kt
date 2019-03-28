package com.inari.team.presentation.ui.logs


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.getFilesList
import com.inari.team.core.utils.getPositionsFilesList
import kotlinx.android.synthetic.main.fragment_logs.*
import javax.inject.Inject


class LogsFragment : BaseFragment() {

    @Inject
    lateinit var navigator: Navigator

    private var adapter: LogsAdapter? = null
    private var positionsAdapter: PositionLogsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)
    }

    private fun setViews(view: View) {

        adapter = LogsAdapter(view.context) {
            layoutEmptyView.visibility = VISIBLE
            rvLogs.visibility = GONE
        }
        rvLogs.layoutManager = LinearLayoutManager(view.context)
        rvLogs.adapter = adapter

        positionsAdapter = PositionLogsAdapter(view.context, {
            layoutEmptyView.visibility = VISIBLE
            rvPositionLogs.visibility = GONE
        }, {
            navigator.navigateToMapLogActivity(it)
        })
        rvPositionLogs.layoutManager = LinearLayoutManager(view.context)
        rvPositionLogs.adapter = positionsAdapter

        swipeRefresh.setOnRefreshListener {
            setFiles()
        }

        setFiles()
    }

    fun setFiles() {
        val filesList = getFilesList()
        val positionsFileList = getPositionsFilesList()

        if (filesList.isEmpty() && positionsFileList.isEmpty()) {
            clLogs.visibility = GONE
            clPositionLogs.visibility = GONE
            layoutEmptyView.visibility = VISIBLE
        } else {
            if (filesList.isNotEmpty()) {
                clLogs.visibility = VISIBLE
                layoutEmptyView.visibility = GONE
                adapter?.setLogs(filesList)
            }

            if (positionsFileList.isNotEmpty()) {
                clPositionLogs.visibility = VISIBLE
                layoutEmptyView.visibility = GONE
                positionsAdapter?.setLogs(positionsFileList)
            }
        }

        swipeRefresh.isRefreshing = false
    }


}
