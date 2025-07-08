package com.subhajitrajak.makautstudybuddy.data.models

data class DeepSeekRequest(
    val model: String = "deepseek/deepseek-r1:free",
    val messages: List<DeepSeekMessage>
)

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: DeepSeekMessage
)