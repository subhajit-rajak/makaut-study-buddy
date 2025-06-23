package com.subhajitrajak.makautstudybuddy.presentation.syllabus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.makautstudybuddy.data.repository.SyllabusRepo

@Suppress("UNCHECKED_CAST")
class SyllabusViewModelFactory (private val repo: SyllabusRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyllabusViewModel(repo) as T
    }
}