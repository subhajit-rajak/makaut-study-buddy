package com.subhajitrajak.makautstudybuddy.presentation.notes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModel
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModelFactory
import com.subhajitrajak.makautstudybuddy.data.models.HomeModel
import com.subhajitrajak.makautstudybuddy.data.repository.NotesRepo
import com.subhajitrajak.makautstudybuddy.databinding.ActivityNotesBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants.TEST_AD_UNIT_ID
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim
import kotlin.getValue

class NotesActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityNotesBinding by lazy {
        ActivityNotesBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<HomeModel>()
    private val adapter = NotesAdapter(list, activity)

    private val repo = NotesRepo(activity)
    private val viewModel by lazy {
        ViewModelProvider(activity, NotesViewModelFactory(repo))[NotesViewModel::class.java]
    }

    private val subscriptionViewModel: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(applicationContext)
    }

    private var adUnitId = if(BuildConfig.DEBUG) TEST_AD_UNIT_ID else BuildConfig.NOTES_ADMOB_UNIT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            rv.adapter=adapter
            viewModel.getHomeData()
            handleHomeBackend()
            mErrorLayout.mTryAgainBtn.setOnClickListener {
                viewModel.getHomeData()
            }

            backButton.setOnClickListener {
                finish()
            }
        }

        subscriptionViewModel.isPremium.observe(this) { premium ->
            if (premium) {
                binding.adView.visibility = View.GONE
            } else {
                loadAd()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleHomeBackend() {
        viewModel.notesLiveData.observe(activity) { it ->
            when (it) {
                is MyResponses.Error -> {
                    binding.mErrorHolder.showWithAnim()
                }
                is MyResponses.Loading -> {}
                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    val tempList = it.data
                    list.clear()
                    tempList?.forEach{
                        list.add(it)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadAd() {
        val adView = AdView(activity)
        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.BANNER)

        binding.adView.removeAllViews()
        binding.adView.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}