package com.subhajitrajak.makautstudybuddy.presentation.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.subhajitrajak.makautstudybuddy.databinding.ActivityPdfBinding
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import androidx.core.graphics.createBitmap

class PdfActivity : AppCompatActivity() {
    private val binding: ActivityPdfBinding by lazy {
        ActivityPdfBinding.inflate(layoutInflater)
    }

    private var currentInputStream: InputStream? = null
    private var isRemote = false
    private var pdfUrlOrPath: String = ""

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

        pdfUrlOrPath = intent.getStringExtra("book_pdf").toString()
        isRemote = intent.getStringExtra("location") != "local"
        log(pdfUrlOrPath)

        binding.apply {
            if (isRemote) {
                loadPdfFromUrl(pdfUrlOrPath)
                askAiButton.visibility = View.GONE
            } else {
                loadPdfFromLocal(pdfUrlOrPath)
            }

            askAiButton.setOnClickListener {
                if (currentInputStream != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val extracted = extractTextFromPdfPage(pdfUrlOrPath, binding.pdfView.currentPage)
                        log("Extracted text: $extracted")

                        // Capture bitmap on UI thread
                        val bitmapBytes = withContext(Dispatchers.Main) {
                            val bitmap = createBitmap(binding.pdfView.width, binding.pdfView.height)
                            val canvas = Canvas(bitmap)
                            binding.pdfView.draw(canvas)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
                            stream.toByteArray()
                        }

                        withContext(Dispatchers.Main) {
                            supportFragmentManager.beginTransaction()
                                .replace(android.R.id.content, PdfAssistantFragment.newInstance(extracted, bitmapBytes))
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                } else {
                    showToast(this@PdfActivity, "PDF not loaded yet.")
                }
            }
        }
    }

    private fun loadPdfFromLocal(path: String) {
        val uri = path.toUri()
        contentResolver.openInputStream(uri)?.let { input ->
            currentInputStream = input
            binding.pdfView.fromStream(input)
                .swipeHorizontal(true)
                .scrollHandle(DefaultScrollHandle(this))
                .enableSwipe(true)
                .pageSnap(true)
                .autoSpacing(true)
                .fitEachPage(true)
                .pageFling(true)
                .load()
        }
    }

    private fun loadPdfFromUrl(url: String) {
        // progress bar until pdf loads
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = fetchPdfStream(url)
            currentInputStream = inputStream
            withContext(Dispatchers.Main) {
                inputStream?.let {
                    binding.pdfView.fromStream(it)
                        .swipeHorizontal(true)
                        .scrollHandle(DefaultScrollHandle(this@PdfActivity))
                        .enableSwipe(true)
                        .pageSnap(true)
                        .autoSpacing(true)
                        .fitEachPage(true)
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

    private fun extractTextFromPdfPage(fileName: String, pageNumber: Int): String {
        return try {
            // Create a PdfReader from InputStream
            val reader = PdfReader(fileName)
            val text = PdfTextExtractor.getTextFromPage(reader, pageNumber + 1).trim()

            reader.close()
            text
        } catch (e: Exception) {
            e.printStackTrace()
            "Unable to extract text"
        }
    }
}