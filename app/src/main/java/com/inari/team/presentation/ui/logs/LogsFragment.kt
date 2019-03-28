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
            setFiles()
        }
        rvLogs.layoutManager = LinearLayoutManager(view.context)
        rvLogs.adapter = adapter

        positionsAdapter = PositionLogsAdapter(view.context, {
            setFiles()
        }, {
            navigator.navigateToMapLogActivity(it)
        })
        rvPositionLogs.layoutManager = LinearLayoutManager(view.context)
        rvPositionLogs.adapter = positionsAdapter

        swipeRefresh.setOnRefreshListener {
            setFiles()
        }

        clPositionLogsTitle.setOnClickListener {
            if (clPositionLogs.visibility == GONE) {
                clPositionLogs.visibility = VISIBLE
            } else {
                clPositionLogs.visibility = GONE
            }
            ivPositionLogsTitle.rotation = ivPositionLogsTitle.rotation + 180
        }

        clLogsTitle.setOnClickListener {
            if (clLogs.visibility == GONE) {
                clLogs.visibility = VISIBLE
            } else {
                clLogs.visibility = GONE
            }
            ivMeasurementTitle.rotation = ivMeasurementTitle.rotation + 180
        }

    }

    fun setFiles() {
        val filesList = getFilesList()
        val positionsFileList = getPositionsFilesList()

        if (filesList.isEmpty() && positionsFileList.isEmpty()) {
            clLogsTitle.visibility = GONE
            clPositionLogsTitle.visibility = GONE
            tvEmptyLogs.visibility = GONE
            tvEmptyPositionLogs.visibility = GONE
            layoutEmptyView.visibility = VISIBLE
        } else {

            layoutEmptyView.visibility = GONE
            clLogsTitle.visibility = VISIBLE
            clPositionLogsTitle.visibility = VISIBLE

            if (filesList.isNotEmpty()) {
                adapter?.setLogs(filesList)
            } else {
                adapter?.clear()
                tvEmptyLogs.visibility = VISIBLE
            }

            if (positionsFileList.isNotEmpty()) {
                positionsAdapter?.setLogs(positionsFileList)
            } else {
                positionsAdapter?.clear()
                tvEmptyPositionLogs.visibility = VISIBLE
            }
        }

        swipeRefresh.isRefreshing = false
    }


}
