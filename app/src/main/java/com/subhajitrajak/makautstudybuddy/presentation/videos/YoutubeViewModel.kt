package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.*
import com.subhajitrajak.makautstudybuddy.data.models.VideoDetails
import com.subhajitrajak.makautstudybuddy.data.repository.YoutubeRepo
import kotlinx.coroutines.launch

class YoutubeViewModel(private val repository: YoutubeRepo) : ViewModel() {

    private val _thumbnailData = MutableLiveData<VideoDetails>()
    val thumbnailData: LiveData<VideoDetails> = _thumbnailData

    fun fetchThumbnail(apiKey: String, playlistId: String) {
        viewModelScope.launch {
            val item = repository.getFirstThumbnail(apiKey, playlistId)
            _thumbnailData.value = VideoDetails(
                title = item?.snippet?.title,
                thumbnailUrl = item?.snippet?.thumbnails?.medium?.url,
                channelTitle = item?.snippet?.channelTitle
            )
        }
    }

    // ✅ New one-time-use callback-based method (to fix duplication issue)
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
