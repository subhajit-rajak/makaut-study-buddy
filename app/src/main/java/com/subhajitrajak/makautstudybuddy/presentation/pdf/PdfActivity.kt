package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.subhajitrajak.makautstudybuddy.databinding.ActivityPdfBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PdfActivity : AppCompatActivity() {
    private val binding: ActivityPdfBinding by lazy {
        ActivityPdfBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.post {
                window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            // For older versions, use deprecated methods
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        setContentView(binding.root)
        supportActionBar?.hide()

        binding.apply {
            val pdf = intent.getStringExtra("book_pdf").toString()
            val location = intent.getStringExtra("location").toString()

            if(location == "local") {
                pdfView.fromUri(Uri.parse(pdf))
                    .swipeHorizontal(true)
                    .scrollHandle(DefaultScrollHandle(this@PdfActivity))
                    .enableSwipe(true)
                    .pageSnap(true)
                    .autoSpacing(true)
                    .pageFling(true)
                    .load()
            } else {
                // progress bar need to be implemented until pdf loads
                loadPdfFromUrl(pdf)
            }
        }
    }

    private fun loadPdfFromUrl(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = fetchPdfStream(url)
            withContext(Dispatchers.Main) {
                inputStream?.let {
                    binding.pdfView.fromStream(it)
                        .swipeHorizontal(true)
                        .scrollHandle(DefaultScrollHandle(this@PdfActivity))
                        .enableSwipe(true)
                        .pageSnap(true)
                        .autoSpacing(true)
                        .pageFling(true)
                        .load()
                }
            }
        }
    }

    private fun fetchPdfStream(urlString: String): InputStream? {
        return try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpsURLConnection
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedInputStream(urlConnection.inputStream)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}