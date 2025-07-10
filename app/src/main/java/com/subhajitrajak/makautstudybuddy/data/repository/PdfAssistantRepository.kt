package com.subhajitrajak.makautstudybuddy.data.repository

import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.data.models.DeepSeekMessage
import com.subhajitrajak.makautstudybuddy.data.models.DeepSeekRequest
import com.subhajitrajak.makautstudybuddy.presentation.pdf.DeepSeekApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PdfAssistantRepository {

    private val api: DeepSeekApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(DeepSeekApiService::class.java)
    }

    suspend fun askDeepSeek(prompt: String, model: String): Result<String> {
        return try {
            val request = DeepSeekRequest(
                model = model,
                messages = listOf(DeepSeekMessage("user", prompt))
            )
            val result = api.chatWithAI(request, "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
            if (result.isSuccessful) {
                val reply = result.body()?.choices?.firstOrNull()?.message?.content ?: "No reply"
                Result.success(reply)
            } else {
                Result.failure(Exception(result.code().toString()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// list of working models -
// "qwen/qwen3-8b:free"
// "deepseek/deepseek-r1:free"