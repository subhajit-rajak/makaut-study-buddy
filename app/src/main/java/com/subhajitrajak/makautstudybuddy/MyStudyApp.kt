package com.subhajitrajak.makautstudybuddy

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyStudyApp :Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}