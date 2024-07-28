package com.subhajitrajak.makautstudybuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.subhajitrajak.makautstudybuddy.adapters.CategoryAdapter
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySubjectsBinding
import com.subhajitrajak.makautstudybuddy.models.BooksModel

class SubjectsActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySubjectsBinding by lazy {
        ActivitySubjectsBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<BooksModel>()
    private val adapter = CategoryAdapter(list, activity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rvSubjects.adapter=adapter
            val bookList = intent.getSerializableExtra("book_list") as ArrayList<*>
            bookList.forEach {
                list.add(it as BooksModel)
            }
        }
    }
}