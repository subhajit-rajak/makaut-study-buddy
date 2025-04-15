package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.data.repository.VideosRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideosViewModel(private val repo: VideosRepo) : ViewModel() {
    val videosLiveData get() = repo.videosLiveData

    fun getVideosData() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getVideosData()
        }
    }
}