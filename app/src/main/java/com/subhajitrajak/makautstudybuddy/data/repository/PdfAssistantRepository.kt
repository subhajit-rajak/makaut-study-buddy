package com.subhajitrajak.makautstudybuddy.data.repository

import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.data.models.OpenRouterMessage
import com.subhajitrajak.makautstudybuddy.data.models.OpenRouterRequest
import com.subhajitrajak.makautstudybuddy.presentation.askAi.OpenRouterApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PdfAssistantRepository {

    private val api: OpenRouterApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(OpenRouterApiService::class.java)
    }

    suspend fun askDeepSeek(prompt: String, model: String): Result<String> {
        return try {
            val request = OpenRouterRequest(
                model = model,
                messages = listOf(OpenRouterMessage("user", prompt))
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