package com.subhajitrajak.makautstudybuddy.data.models

data class OpenRouterRequest(
    val model: String = "deepseek/deepseek-r1:free",
    val messages: List<OpenRouterMessage>
)

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: OpenRouterMessage
)