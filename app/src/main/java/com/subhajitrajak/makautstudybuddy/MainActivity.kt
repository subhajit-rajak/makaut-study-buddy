package com.subhajitrajak.makautstudybuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.subhajitrajak.makautstudybuddy.adapters.HomeAdapter
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.models.BooksModel
import com.subhajitrajak.makautstudybuddy.models.HomeModel

class MainActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val booksList = ArrayList<BooksModel>()
    private val booksList2 = ArrayList<BooksModel>()
    private val list = ArrayList<HomeModel>()
    val adapter = HomeAdapter(list, activity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rv.adapter=adapter
            booksList.add(BooksModel(branch = "CSE", bookName = "sem1Maths", bookPDF = "sample_book.pdf"))
            booksList.add(BooksModel(branch = "CSE", bookName = "sem2DSA", bookPDF = "sample_book.pdf"))
            booksList2.add(BooksModel(branch = "IT", bookName = "sem1Maths", bookPDF = "sample_book.pdf"))
            booksList2.add(BooksModel(branch = "IT", bookName = "sem2DAA", bookPDF = "sample_book.pdf"))
            booksList2.add(BooksModel(branch = "IT", bookName = "sem2Economics", bookPDF = "sample_book.pdf"))

            list.add(HomeModel(branch = "CSE", booksList = booksList))
            list.add(HomeModel(branch = "IT", booksList = booksList2))

        }
    }
}