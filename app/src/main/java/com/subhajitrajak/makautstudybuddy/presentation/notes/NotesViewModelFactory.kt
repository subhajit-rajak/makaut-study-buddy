package com.subhajitrajak.makautstudybuddy.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.NotesRepo

@Suppress("UNCHECKED_CAST")
class NotesViewModelFactory (private val repo: NotesRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotesViewModel(repo) as T
    }
}