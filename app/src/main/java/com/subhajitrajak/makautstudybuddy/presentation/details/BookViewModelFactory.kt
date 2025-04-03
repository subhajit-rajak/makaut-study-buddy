package com.subhajitrajak.makautstudybuddy.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.BookRepo

@Suppress("UNCHECKED_CAST")
class BookViewModelFactory (private val repo: BookRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookViewModel(repo) as T
    }
}