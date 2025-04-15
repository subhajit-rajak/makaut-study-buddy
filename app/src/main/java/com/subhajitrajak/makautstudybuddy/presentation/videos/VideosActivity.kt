package com.subhajitrajak.makautstudybuddy.presentation.videos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.VideosModel
import com.subhajitrajak.makautstudybuddy.data.repository.VideosRepo
import com.subhajitrajak.makautstudybuddy.data.repository.YoutubeRepo
import com.subhajitrajak.makautstudybuddy.databinding.ActivityVideosBinding
import com.subhajitrajak.makautstudybuddy.utils.Constants.VIDEOS_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim

class VideosActivity : AppCompatActivity() {

    private val apiKey = BuildConfig.API_KEY
    private val youtubeRepo = YoutubeRepo(RetrofitInstance.api)
    private val youtubeViewModel by lazy {
        ViewModelProvider(this, YoutubeViewModelFactory(youtubeRepo))[YoutubeViewModel::class.java]
    }

    private val videosRepo = VideosRepo(this)
    private val videosViewModel by lazy {
        ViewModelProvider(this, VideosViewModelFactory(videosRepo))[VideosViewModel::class.java]
    }
    private var videoList = ArrayList<VideosModel>()
    val adapter = VideosAdapter(videoList, this)

    private val binding: ActivityVideosBinding by lazy {
        ActivityVideosBinding.inflate(layoutInflater)
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
            rvVideos.adapter=adapter
            videosViewModel.getVideosData()
            handleBackend()

            pullToRefresh.setOnRefreshListener {
                videosViewModel.getVideosData()
                pullToRefresh.isRefreshing = false
            }

            backButton.setOnClickListener {
                finish()
            }
        }
    }

    private fun handleBackend() {
        videosViewModel.videosLiveData.observe(this) {
            when (it) {
                is MyResponses.Error -> {
                    binding.mErrorHolder.showWithAnim()
                }
                is MyResponses.Loading -> {}
                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    val tempList = it.data ?: return@observe
                    enrichAndDisplayVideos(tempList)
                }
            }
        }
    }

    private fun enrichAndDisplayVideos(videos: List<VideosModel>) {
        videoList.clear()

        for (model in videos) {
            if (model.title.isNullOrEmpty() || model.thumbnailUrl.isNullOrEmpty() || model.channelTitle.isNullOrEmpty()) {
                val playlistId = model.playlistId ?: continue

                youtubeViewModel.fetchThumbnailOnce(apiKey, playlistId) { ytData ->
                    if (ytData != null) {
                        val updatedModel = model.copy(
                            title = ytData.title,
                            thumbnailUrl = ytData.thumbnailUrl,
                            channelTitle = ytData.channelTitle
                        )

                        log("Fetched: ${ytData.title}")
                        updateFirebase(playlistId, updatedModel)

                        runOnUiThread {
                            videoList.add(updatedModel)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                videoList.add(model)
                adapter.notifyDataSetChanged()
            }
        }
    }


    private fun updateFirebase(playlistId: String, updatedModel: VideosModel) {
        val ref = FirebaseDatabase.getInstance().getReference(VIDEOS_DATA)
        ref.orderByChild("playlistId").equalTo(playlistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.setValue(updatedModel)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

}
