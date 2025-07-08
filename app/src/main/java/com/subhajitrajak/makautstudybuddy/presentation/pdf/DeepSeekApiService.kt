package com.subhajitrajak.makautstudybuddy.presentation.pdf

import com.subhajitrajak.makautstudybuddy.data.models.DeepSeekRequest
import com.subhajitrajak.makautstudybuddy.data.models.DeepSeekResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    suspend fun chatWithAI(
        @Body request: DeepSeekRequest,
        @Header("Authorization") auth: String
    ): Response<DeepSeekResponse>
}