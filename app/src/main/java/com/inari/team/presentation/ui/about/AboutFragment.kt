package com.inari.team.presentation.ui.about


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inari.team.BuildConfig
import com.inari.team.R
import com.inari.team.core.base.BaseFragment
import com.inari.team.core.navigator.Navigator
import kotlinx.android.synthetic.main.fragment_about.*
import javax.inject.Inject
import android.content.ActivityNotFoundException




class AboutFragment : BaseFragment() {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.inari.team.R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVersion.text = BuildConfig.VERSION_NAME

        btSeeTutorial.setOnClickListener {
            navigator.navigateToTutorialActivtiy()
        }

        tvContact.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:${getString(com.inari.team.R.string.contact_email)}")
            try {
                startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
            }

        }
    }


}
