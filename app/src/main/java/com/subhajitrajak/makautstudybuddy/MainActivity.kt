package com.subhajitrajak.makautstudybuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.subhajitrajak.makautstudybuddy.adapters.BooksAdapter
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.models.Books

class MainActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<Books>()
    val adapter = BooksAdapter(list, activity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rv.adapter=adapter
            list.add(Books(R.drawable.book_1,"Rich Dad Poor Dad",getString(R.string.description_1),"sample_book.pdf"))
            list.add(Books(R.drawable.book_2,"Atomic Habits",getString(R.string.description_2),"sample_book.pdf"))
            list.add(Books(R.drawable.book_3,"Best Self",getString(R.string.description_3),"sample_book.pdf"))
            list.add(Books(R.drawable.book_4,"How To Be Fine",getString(R.string.description_4),"sample_book.pdf"))
            list.add(Books(R.drawable.book_5,"Out of the Box",getString(R.string.description_5),"sample_book.pdf"))
            list.add(Books(R.drawable.book_6,"Stripped",getString(R.string.description_6),"sample_book.pdf"))
            list.add(Books(R.drawable.book_7,"12 Rules for Life",getString(R.string.description_7),"sample_book.pdf"))
            list.add(Books(R.drawable.book_8,"Readistan",getString(R.string.description_8),"sample_book.pdf"))
            list.add(Books(R.drawable.book_9,"Reclaim Your Heart",getString(R.string.description_9),"sample_book.pdf"))
            list.add(Books(R.drawable.book_10,"Lost Islamic History",getString(R.string.description_10),"sample_book.pdf"))
        }
    }
}