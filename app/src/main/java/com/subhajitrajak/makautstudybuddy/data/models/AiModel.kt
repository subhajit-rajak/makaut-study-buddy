package com.subhajitrajak.makautstudybuddy.data.models

data class AiModel(
    val modelName: String = "",
    val identifier: String = "",
    val isPremium: Boolean = false,
    val isFaster: Boolean = false,
    val order: Int = 0
)