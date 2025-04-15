package com.subhajitrajak.makautstudybuddy.data.models

data class PlaylistResponse(
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val snippet: Snippet
)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val medium: Thumbnail
)

data class Thumbnail(
    val url: String
)