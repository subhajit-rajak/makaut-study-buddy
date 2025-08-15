package com.subhajitrajak.makautstudybuddy.presentation.askAi

import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.data.models.AiModel
import com.subhajitrajak.makautstudybuddy.data.repository.AiModelsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiModelsViewModel(
    private val repository: AiModelsRepository
) : ViewModel() {

    suspend fun getAllModels(): List<AiModel> {
        return withContext(Dispatchers.IO) {
            repository.fetchAiModels()
        }
    }
}