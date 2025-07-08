package com.subhajitrajak.makautstudybuddy.presentation.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.BuildConfig
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PdfAssistantViewModel : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    fun askDeepSeek(prompt: String) {
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://openrouter.ai/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(DeepSeekApiService::class.java)

                val request = DeepSeekRequest(
                    model = "deepseek/deepseek-r1:free",
                    messages = listOf(DeepSeekMessage("user", prompt))
                )

                val result = api.chatWithAI(request, "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
                if (result.isSuccessful) {
                    val reply = result.body()?.choices?.firstOrNull()?.message?.content ?: "No reply"
                    _response.postValue(reply)
                } else {
                    _response.postValue("Error: ${result.code()}")
                }
            } catch (e: Exception) {
                _response.postValue("Exception: ${e.message}")
            }
        }
    }
}
