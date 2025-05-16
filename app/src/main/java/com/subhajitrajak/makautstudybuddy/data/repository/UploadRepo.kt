package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOKS_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.ORGANIZERS_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class UploadRepo(val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val notesDataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(NOTES_DATA)
    private val organizersDataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(ORGANIZERS_DATA)
    private val booksDataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(BOOKS_DATA)
    private val uploadRequestsLD = MutableLiveData<MyResponses<ArrayList<BooksModel>>>()

    val uploadRequestsLiveData get() = uploadRequestsLD

    // Store listeners
    private val notesListener = createListener()
    private val organizersListener = createListener()
    private val booksListener = createListener()

    // Create listener function
    private fun createListener(): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                for (bookSnap in snapshots.children) {
                    val book = bookSnap.getValue(BooksModel::class.java)
                    book?.let {
                        finalList.add(it)
                    }
                }
                checkAndPost()
            }

            override fun onCancelled(error: DatabaseError) {
                uploadRequestsLiveData.postValue(MyResponses.Error("Database error occurred"))
            }
        }
    }

    private val finalList = ArrayList<BooksModel>()
    private var fetchedCount = 0
    private val totalFetches = 3

    private fun checkAndPost() {
        fetchedCount++
        if (fetchedCount == totalFetches) {
            if (finalList.isNotEmpty()) {
                uploadRequestsLiveData.postValue(MyResponses.Success(finalList))
            } else {
                uploadRequestsLiveData.postValue(MyResponses.Error("No Books Found"))
            }
        }
    }

    fun getRequestsData() {
        uploadRequestsLiveData.postValue(MyResponses.Loading())

        // Reset
        finalList.clear()
        fetchedCount = 0

        notesDataRef.addValueEventListener(notesListener)
        organizersDataRef.addValueEventListener(organizersListener)
        booksDataRef.addValueEventListener(booksListener)
    }

    fun removeListeners() {
        notesDataRef.removeEventListener(notesListener)
        organizersDataRef.removeEventListener(organizersListener)
        booksDataRef.removeEventListener(booksListener)
    }
}