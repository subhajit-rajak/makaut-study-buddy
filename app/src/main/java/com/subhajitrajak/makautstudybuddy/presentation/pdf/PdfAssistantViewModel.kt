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
            }.onFailure {
                _response.postValue("Error: ${it.message}")
            }
        }
    }
}