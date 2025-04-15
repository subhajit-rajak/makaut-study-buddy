package com.subhajitrajak.makautstudybuddy.data.repository

import com.subhajitrajak.makautstudybuddy.data.models.PlaylistMetadataItem
import com.subhajitrajak.makautstudybuddy.presentation.videos.YouTubeApiService

class YoutubeRepo(private val api: YouTubeApiService) {
    suspend fun getFirstThumbnail(apiKey: String, playlistId: String): PlaylistMetadataItem? {
        return try {
            val response = api.getPlaylistItems(playlistId = playlistId, apiKey = apiKey)
            response.items.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}