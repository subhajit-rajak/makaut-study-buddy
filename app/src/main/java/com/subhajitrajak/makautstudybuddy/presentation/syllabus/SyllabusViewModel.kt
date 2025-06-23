package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.makautstudybuddy.data.repository.SyllabusRepo
import kotlinx.coroutines.launch

class SyllabusViewModel (private val repo: SyllabusRepo): ViewModel() {
    val syllabusLiveData get() = repo.syllabusLiveData

    fun getSyllabusData() {
        viewModelScope.launch {
            repo.getSyllabusData()
        }
    }
}