package com.subhajitrajak.makautstudybuddy.presentation.subjects

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySubjectsBinding
import com.subhajitrajak.makautstudybuddy.models.BooksModel

@Suppress("DEPRECATION")
class SubjectsActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySubjectsBinding by lazy {
        ActivitySubjectsBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<BooksModel>()
    private val adapter = CategoryAdapter(list, activity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            rvSubjects.adapter=adapter
            val bookList = intent.getSerializableExtra("book_list") as ArrayList<*>
            bookList.forEach {
                list.add(it as BooksModel)
            }

            backButton.setOnClickListener {
                finish()
            }
        }
    }
}