package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.data.models.VideosModel
import com.subhajitrajak.makautstudybuddy.data.repository.VideosRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideosViewModel(private val repo: VideosRepo) : ViewModel() {

    private val _allVideos = MutableLiveData<List<VideosModel>>(emptyList())
    private val _filteredVideos = MutableLiveData<List<VideosModel>>(emptyList())
    val filteredVideos: LiveData<List<VideosModel>> = _filteredVideos

    val videosLiveData get() = repo.videosLiveData

    fun getVideosData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getVideosData()
        }
    }

    fun setVideos(list: List<VideosModel>) {
        val sorted = list.sortedBy { it.title?.lowercase() }
        _allVideos.postValue(sorted)
        _filteredVideos.postValue(sorted)
    }

    fun filterVideos(query: String) {
        val original = _allVideos.value ?: return
        _filteredVideos.postValue(
            if (query.isBlank()) original
            else original.filter {
                it.title?.contains(query, ignoreCase = true) == true || it.channelTitle?.contains(query, ignoreCase = true) == true
            }
        )
    }

    fun addEnrichedVideo(video: VideosModel) {
        val updated = (_allVideos.value ?: emptyList()) + video
        setVideos(updated)
    }
}