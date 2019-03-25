package com.inari.team.presentation.ui.logs


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.utils.getFilesList
import com.inari.team.core.utils.toast
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

        val filesList = getFilesList()

        if (filesList.isEmpty()) {
            toast("There are no files yet")
        }

        adapter = LogsAdapter(view.context)
        rvLogs.layoutManager = LinearLayoutManager(view.context)
        rvLogs.adapter = adapter

        adapter?.setLogs(getFilesList())
    }


}
