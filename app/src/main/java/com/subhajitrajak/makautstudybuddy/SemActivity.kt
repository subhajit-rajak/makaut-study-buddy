package com.subhajitrajak.makautstudybuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.subhajitrajak.makautstudybuddy.adapters.CategoryAdapter
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySemBinding
import com.subhajitrajak.makautstudybuddy.models.BooksModel

class SemActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivitySemBinding by lazy {
        ActivitySemBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<BooksModel>()
    private val adapter = CategoryAdapter(list, activity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rvSemester.adapter=adapter
            val semList = intent.getSerializableExtra("book_list") as ArrayList<*>
            semList.forEach {
                list.add(it as BooksModel)
            }
        }
    }
}