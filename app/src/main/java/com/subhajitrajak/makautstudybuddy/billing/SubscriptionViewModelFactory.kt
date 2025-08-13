package com.subhajitrajak.makautstudybuddy.billing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SubscriptionViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            val repository = SubscriptionRepository(appContext)
            @Suppress("UNCHECKED_CAST")
            return SubscriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}