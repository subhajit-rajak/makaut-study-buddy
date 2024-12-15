package com.subhajitrajak.makautstudybuddy.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.repository.BookRepo

@Suppress("UNCHECKED_CAST")
class BookViewModelFactory (private val repo: BookRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookViewModel(repo) as T
    }
}