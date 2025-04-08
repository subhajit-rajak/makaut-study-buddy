package com.subhajitrajak.makautstudybuddy.presentation.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.UploadRepo

@Suppress("UNCHECKED_CAST")
class UploadViewModelFactory (private val repo: UploadRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UploadViewModel(repo) as T
    }
}