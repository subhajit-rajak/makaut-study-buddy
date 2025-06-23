package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.subhajitrajak.makautstudybuddy.databinding.ActivityPdfBinding
import com.subhajitrajak.makautstudybuddy.utils.showToast
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
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            // For older versions, use deprecated methods
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        setContentView(binding.root)
        supportActionBar?.hide()

        binding.apply {
            val pdf = intent.getStringExtra("book_pdf").toString()
            val location = intent.getStringExtra("location").toString()

            if (location == "local") {
                pdfView.fromUri(Uri.parse(pdf))
                    .swipeHorizontal(true)
                    .scrollHandle(DefaultScrollHandle(this@PdfActivity))
                    .enableSwipe(true)
                    .pageSnap(true)
                    .autoSpacing(true)
                    .pageFling(true)
                    .load()
            } else {
                loadPdfFromUrl(pdf)
            }
        }
    }

    private fun loadPdfFromUrl(url: String) {
        // progress bar until pdf loads
        binding.progressBar.visibility = View.VISIBLE
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
                        .onLoad {
                            binding.progressBar.visibility = View.GONE
                        }
                        .onError {
                            // handle error
                            showToast(this@PdfActivity, "Some unknown error occurred")
                            binding.progressBar.visibility = View.GONE
                        }
                        .load()
                } ?: run {
                    // Handle null stream
                    showToast(this@PdfActivity, "Failed to fetch File. Please try again or check your internet connected")
                    binding.progressBar.visibility = View.GONE
                }
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