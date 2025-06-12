package com.subhajitrajak.makautstudybuddy.presentation.books

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.data.repository.BookRepo
import kotlinx.coroutines.launch

class BooksViewModel(private val repo: BookRepo) : ViewModel() {
    private val _allBooks = MutableLiveData<List<BooksModel>>(emptyList())
    private val _filteredBooks = MutableLiveData<List<BooksModel>>(emptyList())
    val filteredBooks: LiveData<List<BooksModel>> = _filteredBooks

    val booksLiveData get() = repo.booksLiveData

    fun getBooksData() {
        viewModelScope.launch {
            repo.getBooksData()
        }
    }

    fun setBooks(list: List<BooksModel>) {
        val sorted = list.sortedBy { it.bookName.lowercase() }
        _allBooks.postValue(sorted)
        _filteredBooks.postValue(sorted)
    }

    fun filterBooks(query: String) {
        val original = _allBooks.value ?: return
        _filteredBooks.postValue(
            if (query.isBlank()) original
            else original.filter {
                it.bookName.contains(query, ignoreCase = true) || it.authorName?.contains(query, ignoreCase = true) == true
            }
        )
    }
}