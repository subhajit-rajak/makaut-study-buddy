package com.subhajitrajak.makautstudybuddy.presentation.videos

import com.subhajitrajak.makautstudybuddy.data.models.PlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("playlists")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet",
        @Query("id") playlistId: String,
        @Query("key") apiKey: String
    ): PlaylistResponse
}