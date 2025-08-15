package com.subhajitrajak.makautstudybuddy.presentation.askAi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.AiModelsRepository

class AiModelsViewModelFactory(
    private val repository: AiModelsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiModelsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiModelsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
