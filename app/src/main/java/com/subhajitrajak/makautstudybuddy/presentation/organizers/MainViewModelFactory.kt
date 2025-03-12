package com.subhajitrajak.makautstudybuddy.presentation.organizers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.repository.MainRepo

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory (private val repo: MainRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repo) as T
    }
}