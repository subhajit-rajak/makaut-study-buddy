package com.subhajitrajak.makautstudybuddy.viewModels

import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.repository.MainRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(val repo: MainRepo) : ViewModel() {
    val homeLiveData get() = repo.homeLiveData

    fun getHomeData() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getHomeData()
        }
    }

}