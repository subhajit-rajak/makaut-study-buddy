package com.subhajitrajak.makautstudybuddy.presentation.videos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.subhajitrajak.makautstudybuddy.utils.Constants.TEST_AD_UNIT_ID
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.Constants.VIDEOS_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showToast
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

    private var adUnitId = if(BuildConfig.DEBUG) TEST_AD_UNIT_ID else BuildConfig.VIDEOS_ADMOB_UNIT_ID

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
            rvVideos.adapter = adapter
            videosViewModel.getVideosData()
            handleBackend()

            pullToRefresh.setOnRefreshListener {
                videosViewModel.getVideosData()
                pullToRefresh.isRefreshing = false
            }

            backButton.setOnClickListener {
                finish()
            }

            uploadButton.setOnClickListener {
                val bottomSheetView =
                    layoutInflater.inflate(R.layout.bottom_sheet_upload_video, binding.root, false)
                val dialog = BottomSheetDialog(this@VideosActivity)
                dialog.setContentView(bottomSheetView)

                val editTextPlaylistId =
                    bottomSheetView.findViewById<EditText>(R.id.editTextPlaylistId)
                val submitButton = bottomSheetView.findViewById<Button>(R.id.buttonSubmit)

                submitButton.setOnClickListener {
                    val playlistLink = editTextPlaylistId.text.toString().trim()
                    val playlistId = extractPlaylistId(playlistLink)
                    if (!playlistId.isNullOrEmpty()) {
                        log("Submitting playlist ID: $playlistId")
                        uploadPlaylistRequest(playlistId)
                        dialog.dismiss()
                    } else {
                        editTextPlaylistId.error = "Playlist link is either not valid or empty"
                    }
                }
                dialog.show()
            }

            searchBar.addTextChangedListener {
                val query = it.toString().trim()
                videosViewModel.filterVideos(query)
            }
        }

        loadAd()
    }

    private fun extractPlaylistId(url: String): String? {
        val regex = Regex("[?&]list=([a-zA-Z0-9_-]+)")
        val match = regex.find(url)
        return match?.groupValues?.get(1)
    }

    private fun uploadPlaylistRequest(playlistId: String) {
        val videoModel = VideosModel(
            playlistId = playlistId
        )

        val uploadRequestRef = FirebaseDatabase.getInstance().getReference(UPLOAD_REQUESTS)
        uploadRequestRef.child(VIDEOS_DATA).child(playlistId).setValue(videoModel)
            .addOnSuccessListener {
                showToast(this, "Request has been Sent")
            }
            .addOnFailureListener {
                showToast(this, "Failed to send request")
            }
    }

    private fun handleBackend() {
        videosViewModel.filteredVideos.observe(this) {
            adapter.updateData(it)
        }

        videosViewModel.videosLiveData.observe(this) {
            when (it) {
                is MyResponses.Error -> {
                    binding.mErrorHolder.showWithAnim()
                    binding.rvVideos.removeWithAnim()
                }

                is MyResponses.Loading -> { }

                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    binding.rvVideos.showWithAnim()
                    val tempList = it.data ?: return@observe
                    videosViewModel.setVideos(tempList)
                    enrichMissingData(tempList)
                }
            }
        }

    }

    private fun enrichMissingData(videos: List<VideosModel>) {
        val pending = videos.filter {
            it.title.isNullOrEmpty() || it.thumbnailUrl.isNullOrEmpty() || it.channelTitle.isNullOrEmpty()
        }

        for (model in pending) {
            val playlistId = model.playlistId ?: continue
            youtubeViewModel.fetchThumbnailOnce(apiKey, playlistId) { ytData ->
                if (ytData != null) {
                    val updatedModel = model.copy(
                        title = ytData.title,
                        thumbnailUrl = ytData.thumbnailUrl,
                        channelTitle = ytData.channelTitle
                    )

                    updateFirebase(playlistId, updatedModel)

                    runOnUiThread {
                        videosViewModel.addEnrichedVideo(updatedModel)
                    }
                }
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

    private fun loadAd() {
        val adView = AdView(this)
        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.BANNER)

        binding.adView.removeAllViews()
        binding.adView.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}