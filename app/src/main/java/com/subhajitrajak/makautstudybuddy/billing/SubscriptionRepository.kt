package com.subhajitrajak.makautstudybuddy.billing

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.subhajitrajak.makautstudybuddy.utils.Constants

class SubscriptionRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("subscription_prefs", Context.MODE_PRIVATE)

    fun updatePremiumFromCustomerInfo(onUpdated: (Boolean) -> Unit) {
        Purchases.sharedInstance.getCustomerInfoWith({ _ ->
            onUpdated(loadPremium())
        }) { customerInfo ->
            val premium = isPremium(customerInfo)
            savePremium(premium)
            onUpdated(premium)
        }
    }

    fun isPremium(customerInfo: CustomerInfo): Boolean {
        val entitlementId = Constants.ENTITLEMENT_PREMIUM
        return customerInfo.entitlements[entitlementId]?.isActive == true
    }

    private fun savePremium(isPremium: Boolean) {
        prefs.edit { putBoolean(KEY_IS_PREMIUM, isPremium) }
    }

    fun loadPremium(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
    }
}