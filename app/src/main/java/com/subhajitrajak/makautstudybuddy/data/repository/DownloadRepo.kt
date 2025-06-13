package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.subhajitrajak.makautstudybuddy.presentation.details.DownloaderRetrofitInstance
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DownloadRepo(private val context: Context) {

    private val downloadLd = MutableLiveData<MyResponses<DownloadModel>>()
    val downloadLiveData get() = downloadLd

    private var downloadJob: Job? = null

    fun downloadPdf(url: String, fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            val model = DownloadModel(100, true, -1, file.toURI().toString())
            downloadLiveData.postValue(MyResponses.Success(model))
            return
        }

        downloadLiveData.postValue(MyResponses.Loading())

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = DownloaderRetrofitInstance.downloadService.downloadFile(url)

                val inputStream: InputStream = response.byteStream()
                val outputStream: OutputStream = FileOutputStream(file)

                val totalSize = response.contentLength()
                val buffer = ByteArray(4096)
                var downloaded = 0L
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1 && isActive) {
                    outputStream.write(buffer, 0, read)
                    downloaded += read

                    val progress = (downloaded * 100 / totalSize).toInt()
                    downloadLiveData.postValue(MyResponses.Loading(progress))
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                if (isActive) {
                    downloadLiveData.postValue(
                        MyResponses.Success(
                            DownloadModel(100, true, -1, file.toURI().toString())
                        )
                    )
                } else {
                    // If the coroutine was cancelled mid-download
                    file.delete()
                    downloadLiveData.postValue(MyResponses.Error("Download cancelled"))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                downloadLiveData.postValue(MyResponses.Error("Failed to download $fileName.\nReason: ${e.message}"))
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    data class DownloadModel(
        val progress: Int = 0,
        val isDownloaded: Boolean,
        val downloadId: Long,
        val filePath: String
    )
}