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

    fun askDeepSeek(prompt: String) {
        viewModelScope.launch {
            _response.postValue("Thinking...")
            val result = repository.askDeepSeek(prompt)
            result.onSuccess {
                _response.postValue(it)
            }.onFailure { error ->
                val friendlyMessage = when (error.message) {
                    "Error: 400" -> "That didn't go through properly. Please try again."
                    "Error: 401" -> "Authentication issue. Please log in again or switch models if the issue continues."
                    "Error: 402" -> "We’re out of credits at the moment. Please try again later—this should be resolved soon."
                    "Error: 403" -> "Your input was flagged. Try rephrasing it slightly and resubmit."
                    "Error: 408" -> "That took too long to process. Please check your connection or try again shortly."
                    "Error: 429" -> "You’re sending messages too quickly. Give it a moment and try again."
                    "Error: 502" -> "The AI model is temporarily unavailable. Please try again shortly."
                    "Error: 503" -> "No model is available to handle this request right now. Try again later."
                    else -> "Something went wrong (${error.message}). Please try again."
                }
                _response.postValue(friendlyMessage)
            }
        }
    }
}