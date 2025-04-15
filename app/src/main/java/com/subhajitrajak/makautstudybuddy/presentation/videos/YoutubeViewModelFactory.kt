package com.subhajitrajak.makautstudybuddy.presentation.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.YoutubeRepo

@Suppress("UNCHECKED_CAST")
class YoutubeViewModelFactory(private val repository: YoutubeRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return YoutubeViewModel(repository) as T
    }
}