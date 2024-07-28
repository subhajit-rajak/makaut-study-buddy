package com.subhajitrajak.makautstudybuddy

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.subhajitrajak.makautstudybuddy.databinding.ActivityPdfBinding

class PdfActivity : AppCompatActivity() {
    private val binding: ActivityPdfBinding by lazy {
        ActivityPdfBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.apply {
            val pdf = intent.getStringExtra("book_pdf").toString()
            pdfView.fromUri(Uri.parse(pdf))
                .swipeHorizontal(true)
                .enableSwipe(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .load()
        }
    }
}