package com.subhajitrajak.makautstudybuddy.presentation.notes

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.HomeModel
import com.subhajitrajak.makautstudybuddy.data.repository.NotesRepo
import com.subhajitrajak.makautstudybuddy.databinding.ActivityNotesBinding
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim

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
}