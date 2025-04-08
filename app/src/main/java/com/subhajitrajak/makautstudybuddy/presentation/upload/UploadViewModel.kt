package com.subhajitrajak.makautstudybuddy.presentation.upload

import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.data.repository.UploadRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadViewModel(private val repo: UploadRepo) : ViewModel() {
    val uploadRequestsLiveData get() = repo.uploadRequestsLiveData

    fun getRequestsData() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getRequestsData()
        }
    }

}