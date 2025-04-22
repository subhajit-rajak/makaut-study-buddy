package com.subhajitrajak.makautstudybuddy.data.models

import com.subhajitrajak.makautstudybuddy.utils.Constants.PENDING
import com.subhajitrajak.makautstudybuddy.utils.Constants.VIDEOS

data class VideosModel( // from firebase response
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val channelTitle: String? = null,

    val playlistId: String? = null,
    val type: String = VIDEOS,
    val status: String = PENDING
)