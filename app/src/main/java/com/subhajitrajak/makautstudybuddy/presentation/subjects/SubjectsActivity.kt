package com.subhajitrajak.makautstudybuddy.presentation.subjects

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySubjectsBinding

@Suppress("DEPRECATION")
class SubjectsActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySubjectsBinding by lazy {
        ActivitySubjectsBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<BooksModel>()
    private lateinit var adapter: CategoryAdapter
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
            adapter = CategoryAdapter(list, activity)
            rvSubjects.adapter=adapter
            val bookList = intent.getSerializableExtra("book_list") as ArrayList<*>
            bookList.forEach {
                list.add(it as BooksModel)
            }

            backButton.setOnClickListener {
                finish()
            }

            // Handle chip selection
            chipGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedChip = group.findViewById<Chip>(checkedId)
                val chipText = selectedChip.text.toString()

                if (chipText == "All") {
                    adapter = CategoryAdapter(list, activity)
                    rvSubjects.adapter = adapter
                } else {
                    val selectedSem = chipText.split("-").last() // Extracts "1" from "Sem-1"
                    val filtered = list.filter { it.semester == selectedSem }
                    adapter = CategoryAdapter(ArrayList(filtered), activity)
                    rvSubjects.adapter = adapter
                }
            }
        }
    }
}