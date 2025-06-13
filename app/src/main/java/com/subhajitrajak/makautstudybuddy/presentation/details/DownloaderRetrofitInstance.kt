package com.subhajitrajak.makautstudybuddy.presentation.details

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DownloaderRetrofitInstance {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/") // required but overridden by @Url
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val downloadService: FileDownloadService = retrofit.create(FileDownloadService::class.java)
}
