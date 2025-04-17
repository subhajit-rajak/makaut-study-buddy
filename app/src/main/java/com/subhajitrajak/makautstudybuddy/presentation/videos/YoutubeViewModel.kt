package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.data.models.VideoDetails
import com.subhajitrajak.makautstudybuddy.data.repository.YoutubeRepo
import kotlinx.coroutines.launch

class YoutubeViewModel(private val repository: YoutubeRepo) : ViewModel() {

    private val _thumbnailData = MutableLiveData<VideoDetails>()
    val thumbnailData: LiveData<VideoDetails> = _thumbnailData

    fun fetchThumbnailOnce(
        apiKey: String,
        playlistId: String,
        onComplete: (VideoDetails?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val item = repository.getFirstThumbnail(apiKey, playlistId)
                if (item != null) {
                    val videoDetails = VideoDetails(
                        title = item.snippet.title,
                        thumbnailUrl = item.snippet.thumbnails.medium.url,
                        channelTitle = item.snippet.channelTitle
                    )
                    onComplete(videoDetails)
                } else {
                    onComplete(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }
}
