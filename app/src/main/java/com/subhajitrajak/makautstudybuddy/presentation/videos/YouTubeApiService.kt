package com.subhajitrajak.makautstudybuddy.presentation.videos

import com.subhajitrajak.makautstudybuddy.data.models.PlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 1
    ): PlaylistResponse
}