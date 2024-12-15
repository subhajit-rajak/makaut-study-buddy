package com.subhajitrajak.makautstudybuddy.models

import java.io.Serializable

data class SettingsModel (
    val rateUs: String? = null,
    val shareApp: String? = null,
    val privacyPolicy: String? = null,
    val terms: String? = null,
    val contact: String? = null,
    val feedback: String? = null
)