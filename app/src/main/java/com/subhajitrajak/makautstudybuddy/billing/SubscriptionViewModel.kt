package com.subhajitrajak.makautstudybuddy.billing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SubscriptionViewModel(private val repository: SubscriptionRepository) : ViewModel() {
    private val _isPremium: MutableLiveData<Boolean> = MutableLiveData(repository.loadPremium())
    val isPremium: LiveData<Boolean> = _isPremium

    fun refresh() {
        repository.updatePremiumFromCustomerInfo { premium ->
            _isPremium.postValue(premium)
        }
    }
}