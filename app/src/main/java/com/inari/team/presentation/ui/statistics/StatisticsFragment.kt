package com.inari.team.presentation.ui.statistics


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import com.inari.team.presentation.ui.statisticsdetail.StatisticsDetailActivity
import kotlinx.android.synthetic.main.fragment_statistics.*
import javax.inject.Inject


class StatisticsFragment : BaseFragment(), View.OnClickListener {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)

        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardRMS.setOnClickListener(this)
        cardCNO.setOnClickListener(this)
        cardMap.setOnClickListener(this)
        cardGraph4.setOnClickListener(this)
        cardGraph5.setOnClickListener(this)
        cardGraph6.setOnClickListener(this)

        fabOptions.setOnClickListener {
            navigator.navigateToModesActivity()
        }

    }

    override fun onClick(v: View?) {
        v?.let { view ->
            val i = Intent(view.context, StatisticsDetailActivity::class.java)

            when (view.id) {

                R.id.cardRMS -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.RMS)
                }
                R.id.cardCNO -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.CNO)
                }
                R.id.cardMap -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.MAP)
                }
                R.id.cardGraph4 -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.GRAPH4)
                }
                R.id.cardGraph5 -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.GRAPH5)
                }
                R.id.cardGraph6 -> {
                    i.putExtra(StatisticsDetailActivity.GRAPH_TYPE, StatisticsDetailActivity.GRAPH6)
                }
            }

            startActivity(i)

        }


    }


}
