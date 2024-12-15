package com.subhajitrajak.makautstudybuddy

import android.net.Uri
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.subhajitrajak.makautstudybuddy.databinding.ActivityPdfBinding

class PdfActivity : AppCompatActivity() {
    private val binding: ActivityPdfBinding by lazy {
        ActivityPdfBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.post {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.apply {
            val pdf = intent.getStringExtra("book_pdf").toString()
            pdfView.fromUri(Uri.parse(pdf))
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