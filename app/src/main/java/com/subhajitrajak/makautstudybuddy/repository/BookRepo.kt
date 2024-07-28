package com.subhajitrajak.makautstudybuddy.repository

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import java.io.File

class BookRepo(private val context: Context) {

    private val downloadLd = MutableLiveData<MyResponses<DownloadModel>>()
    val downloadLiveData get() = downloadLd

    @SuppressLint("Range")
    suspend fun downloadPdf(url: String, fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            val model = DownloadModel(100, true, -1, file.toURI().toString())
            downloadLiveData.postValue(MyResponses.Success(model))
            return
        }

        downloadLiveData.postValue(MyResponses.Loading())

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadRequest = DownloadManager.Request(Uri.parse(url)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            setTitle(fileName)
            setDescription("Downloading PDF")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverRoaming(false)
            setAllowedOverMetered(true)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val downloadId = downloadManager.enqueue(downloadRequest)
        var isDownloaded = false
        var progress = 0
        while (!isDownloaded) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor.moveToNext()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        val totalSize =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (totalSize > 0) {
                            val downloadedByteSize =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloadedByteSize * 100 / totalSize).toInt()
                            downloadLiveData.postValue(MyResponses.Loading(progress))
                        }

                    }

                    DownloadManager.STATUS_PENDING -> {
                        downloadLiveData.postValue(MyResponses.Loading())
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        downloadLiveData.postValue(MyResponses.Loading())
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isDownloaded = true
                        progress = 100
                        val filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))

                        val model = DownloadModel(progress, isDownloaded, downloadId, filePath)
                        downloadLiveData.postValue(MyResponses.Success(model))
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                        isDownloaded = true
                        downloadLiveData.postValue(MyResponses.Error("Failed to download $fileName.\nReason: $reason"))
                    }
                }
            }
        }
    }
    data class DownloadModel(
        val progress: Int = 0,
        val isDownloaded: Boolean,
        val downloadId: Long,
        val filePath: String
    )
}