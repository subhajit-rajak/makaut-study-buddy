package com.subhajitrajak.makautstudybuddy

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
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
    }
}