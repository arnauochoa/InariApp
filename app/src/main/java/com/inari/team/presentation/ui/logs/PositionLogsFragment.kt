package com.inari.team.presentation.ui.logs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.core.utils.getPositionsFilesList
import kotlinx.android.synthetic.main.fragment_position_logs.*
import javax.inject.Inject

class PositionLogsFragment : BaseFragment() {

    @Inject
    lateinit var navigator: Navigator

    private var positionsAdapter: PositionLogsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_position_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        setFiles()
    }

    fun setFiles() {
        val positionsFileList = getPositionsFilesList()

        positionsAdapter?.setLogs(positionsFileList)
        if (positionsFileList.isEmpty()) {
            layoutEmptyView.visibility = View.VISIBLE
        } else {
            layoutEmptyView.visibility = GONE
        }

        swipeRefresh.isRefreshing = false
    }

}
