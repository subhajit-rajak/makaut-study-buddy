package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.VideosRepo

@Suppress("UNCHECKED_CAST")
class VideosViewModelFactory (private val repo: VideosRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideosViewModel(repo) as T
    }
}