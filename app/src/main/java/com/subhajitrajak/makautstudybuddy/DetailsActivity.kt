package com.subhajitrajak.makautstudybuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subhajitrajak.makautstudybuddy.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityDetailsBinding by lazy {
        ActivityDetailsBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val title = intent.getStringExtra("book_title").toString()
        val desc = intent.getStringExtra("book_desc").toString()
        val pdf = intent.getStringExtra("book_pdf").toString()
        val image = intent.getIntExtra("book_image", 0)

        binding.apply {
            mBookTitle.text = title
            mBookDesc.text = desc
            mBookImage.setImageResource(image)

            mReadBookBtn.setOnClickListener {
                val intent = Intent(activity, PdfActivity::class.java)
                intent.putExtra("book_pdf", pdf)
                startActivity(intent)
            }
        }
    }
}