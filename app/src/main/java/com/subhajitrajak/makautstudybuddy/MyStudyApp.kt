package com.subhajitrajak.makautstudybuddy

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.google.firebase.database.FirebaseDatabase
import com.revenuecat.purchases.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyStudyApp :Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MyStudyApp) {}
        }

        // Initialize RevenueCat
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
        )
    }
}