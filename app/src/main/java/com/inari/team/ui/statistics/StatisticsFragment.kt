package com.inari.team.ui.statistics


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_statistics.*


class StatisticsFragment : Fragment(), View.OnClickListener {
    companion object {

        const val FRAG_TAG = "statistics_fragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardRMS.setOnClickListener(this)
        //...

    }

    override fun onClick(v: View?) {
        v?.let {view ->
            val i = Intent(view.context, StatisticsActivity::class.java)

            when(view.id){
                R.id.cardCN0 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.CNO)
                }
                R.id.cardCN0 ->{

                }
                R.id.cardCN0 ->{

                }
                R.id.cardCN0 ->{

                }
                R.id.cardCN0 ->{}
            }

            startActivity(i)

        }


    }


}
