package com.inari.team.ui.statistics


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< HEAD
<<<<<<< HEAD
import com.inari.team.R
=======
>>>>>>> f69f812... add statistics activity
=======
import com.inari.team.R
>>>>>>> 9f9532e... modes spinner added
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
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 9f9532e... modes spinner added
        cardCNO.setOnClickListener(this)
        cardMap.setOnClickListener(this)
        cardGraph4.setOnClickListener(this)
        cardGraph5.setOnClickListener(this)
        cardGraph6.setOnClickListener(this)
<<<<<<< HEAD
=======
        //...

>>>>>>> f69f812... add statistics activity
=======
>>>>>>> 9f9532e... modes spinner added
    }

    override fun onClick(v: View?) {
        v?.let {view ->
            val i = Intent(view.context, StatisticsActivity::class.java)

            when(view.id){
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 9f9532e... modes spinner added
                R.id.cardRMS ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.RMS)
                }
                R.id.cardCNO ->{
<<<<<<< HEAD
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.CNO)
                }
                R.id.cardMap ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.MAP)
                }
                R.id.cardGraph4 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH4)
                }
                R.id.cardGraph5 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH5)
                }
                R.id.cardGraph6 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH6)
                }
=======
                R.id.cardCN0 ->{
=======
>>>>>>> 9f9532e... modes spinner added
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.CNO)
                }
                R.id.cardMap ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.MAP)
                }
                R.id.cardGraph4 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH4)
                }
                R.id.cardGraph5 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH5)
                }
                R.id.cardGraph6 ->{
                    i.putExtra(StatisticsActivity.GRAPH_TYPE, StatisticsActivity.GRAPH6)
                }
<<<<<<< HEAD
                R.id.cardCN0 ->{}
>>>>>>> f69f812... add statistics activity
=======
>>>>>>> 9f9532e... modes spinner added
            }

            startActivity(i)

        }


    }


}
