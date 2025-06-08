package com.subhajitrajak.makautstudybuddy.presentation.books

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.data.repository.BookRepo
import com.subhajitrajak.makautstudybuddy.databinding.ActivityBooksBinding
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim

class BooksActivity : AppCompatActivity() {

    private val bookRepo = BookRepo(this)
    private val booksViewModel by lazy {
        ViewModelProvider(this, BooksViewModelFactory(bookRepo))[BooksViewModel::class.java]
    }
    private var bookList = ArrayList<BooksModel>()
    val adapter = BooksAdapter(bookList, this)

    private val binding: ActivityBooksBinding by lazy {
        ActivityBooksBinding.inflate(layoutInflater)
    }

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
            rvBooks.adapter = adapter
            booksViewModel.getBooksData()
            handleBackend()

            pullToRefresh.setOnRefreshListener {
                booksViewModel.getBooksData()
                pullToRefresh.isRefreshing = false
            }

            backButton.setOnClickListener {
                finish()
            }

            searchBar.addTextChangedListener {
                val query = it.toString().trim()
                booksViewModel.filterBooks(query)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleBackend() {
        booksViewModel.filteredBooks.observe(this) {
            adapter.updateData(it)
        }

        booksViewModel.booksLiveData.observe(this) {
            when (it) {
                is MyResponses.Error -> {
                    binding.mErrorHolder.showWithAnim()
                    binding.rvBooks.removeWithAnim()
                }

                is MyResponses.Loading -> { }

                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    binding.rvBooks.showWithAnim()
                    val tempList = it.data ?: return@observe
                    bookList.clear()
                    tempList.forEach{ book->
                        bookList.add(book)
                    }
                    booksViewModel.setBooks(bookList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}