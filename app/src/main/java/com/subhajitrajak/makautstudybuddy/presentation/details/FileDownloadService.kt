package com.subhajitrajak.makautstudybuddy.presentation.details

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileDownloadService {
    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl: String): ResponseBody
}