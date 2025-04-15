package com.subhajitrajak.makautstudybuddy.data.models

import com.subhajitrajak.makautstudybuddy.utils.Constants.VIDEOS

data class VideosModel( // from firebase response
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val channelTitle: String? = null,

    val playlistId: String? = null,
    val semester: String? = null,
    val branch: String? = null,
    val type: String = VIDEOS
)