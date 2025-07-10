package com.subhajitrajak.makautstudybuddy.presentation.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.data.repository.PdfAssistantRepository
import kotlinx.coroutines.launch

class PdfAssistantViewModel(
    private val repository: PdfAssistantRepository = PdfAssistantRepository()
) : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    private var selectedModel = "deepseek/deepseek-r1:free"

    fun setModel(model: String) {
        selectedModel = model
    }

    fun askDeepSeek(prompt: String) {
        viewModelScope.launch {
            _response.postValue("Thinking...")
            val result = repository.askDeepSeek(prompt, selectedModel)
            result.onSuccess {
                _response.postValue(it)
            }.onFailure { error ->
                val friendlyMessage = when (error.message) {
                    "400" -> "That didn't go through properly. Please try again."
                    "401" -> "Authentication issue. Please log in again or switch models if the issue continues."
                    "402" -> "We’re out of credits at the moment. Please try again later—this should be resolved soon."
                    "403" -> "Your input was flagged. Try rephrasing it slightly and resubmit."
                    "408" -> "That took too long to process. Please check your connection or try again shortly."
                    "429" -> "You’re sending messages too quickly. Give it a moment and try again."
                    "502" -> "The AI model is temporarily unavailable. Please try again shortly."
                    "503" -> "No model is available to handle this request right now. Try again later."
                    else -> "Something went wrong (${error.message}). Please try again."
                }
                _response.postValue(friendlyMessage)
            }
        }
    }
}