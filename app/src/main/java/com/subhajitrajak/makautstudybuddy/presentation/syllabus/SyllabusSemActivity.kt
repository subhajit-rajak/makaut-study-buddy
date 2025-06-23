package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.data.models.SyllabusSemModel
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySyllabusSemBinding
import com.subhajitrajak.makautstudybuddy.presentation.subjects.CategoryAdapter

class SyllabusSemActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySyllabusSemBinding by lazy {
        ActivitySyllabusSemBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<SyllabusSemModel>()
    private lateinit var adapter: SyllabusSemAdapter

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
            adapter = SyllabusSemAdapter(list, activity)
            rvSem.adapter=adapter
            val bookList = intent.getSerializableExtra("sem_list") as ArrayList<*>
            bookList.forEach {
                list.add(it as SyllabusSemModel)
            }
            list.sortBy { it.semester }

            backButton.setOnClickListener {
                finish()
            }
        }
    }
}