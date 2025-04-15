package com.subhajitrajak.makautstudybuddy.data.models

data class PlaylistResponse(
    val items: List<PlaylistMetadataItem>
)

data class PlaylistMetadataItem(
    val snippet: PlaylistSnippet
)

data class PlaylistSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: PlaylistThumbnails
)

data class PlaylistThumbnails(
    val medium: PlaylistThumbnail
)

data class PlaylistThumbnail(
    val url: String
)