package com.subhajitrajak.makautstudybuddy.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.DownloadRepo

@Suppress("UNCHECKED_CAST")
class DownloadViewModelFactory (private val repo: DownloadRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DownloadViewModel(repo) as T
    }
}