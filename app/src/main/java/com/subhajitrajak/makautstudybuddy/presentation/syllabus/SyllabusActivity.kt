package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.SyllabusModel
import com.subhajitrajak.makautstudybuddy.data.repository.SyllabusRepo
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySyllabusBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants.TEST_AD_UNIT_ID
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim

class SyllabusActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySyllabusBinding by lazy {
        ActivitySyllabusBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<SyllabusModel>()
    private val adapter = SyllabusAdapter(list, activity)

    private val repo = SyllabusRepo(activity)
    private val viewModel by lazy {
        ViewModelProvider(activity, SyllabusViewModelFactory(repo))[SyllabusViewModel::class.java]
    }

    private var adUnitId = if(BuildConfig.DEBUG) TEST_AD_UNIT_ID else BuildConfig.SYLLABUS_ADMOB_UNIT_ID

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
            viewModel.getSyllabusData()
            handleHomeBackend()

            pullToRefresh.setOnRefreshListener {
                viewModel.getSyllabusData()
                pullToRefresh.isRefreshing = false
            }

            mErrorLayout.mTryAgainBtn.setOnClickListener {
                viewModel.getSyllabusData()
            }

            backButton.setOnClickListener {
                finish()
            }
        }

        loadAd()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleHomeBackend() {
        viewModel.syllabusLiveData.observe(activity) { it ->
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