package com.subhajitrajak.makautstudybuddy.presentation.notes

import androidx.lifecycle.ViewModel
import com.subhajitrajak.makautstudybuddy.data.repository.NotesRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(private val repo: NotesRepo) : ViewModel() {
    val notesLiveData get() = repo.notesLiveData

    fun getHomeData() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.getNotesData()
        }
    }
}