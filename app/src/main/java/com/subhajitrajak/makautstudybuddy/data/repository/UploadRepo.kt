package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.ORGANIZERS_DATA
import com.subhajitrajak.makautstudybuddy.utils.Constants.UPLOAD_REQUESTS
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class UploadRepo(val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val notesDataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(NOTES_DATA)
    private val organizersDataRef = firebaseDatabase.getReference(UPLOAD_REQUESTS).child(ORGANIZERS_DATA)
    private val uploadRequestsLD = MutableLiveData<MyResponses<ArrayList<BooksModel>>>()

    val uploadRequestsLiveData get() = uploadRequestsLD

    suspend fun getRequestsData() {
        uploadRequestsLiveData.postValue(MyResponses.Loading())

        val finalList = ArrayList<BooksModel>()
        var fetchedCount = 0
        val totalFetches = 2

        fun checkAndPost() {
            fetchedCount++
            if (fetchedCount == totalFetches) {
                if (finalList.isNotEmpty()) {
                    uploadRequestsLiveData.postValue(MyResponses.Success(finalList))
                } else {
                    uploadRequestsLiveData.postValue(MyResponses.Error("No Books Found"))
                }
            }
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                for (branchSnapshot in snapshots.children) {
                    val booksListSnapshot = branchSnapshot.child("booksList")
                    for (bookSnap in booksListSnapshot.children) {
                        val book = bookSnap.getValue(BooksModel::class.java)
                        book?.let {
                            finalList.add(it)
                        }
                    }
                }
                checkAndPost()
            }

            override fun onCancelled(error: DatabaseError) {
                uploadRequestsLiveData.postValue(MyResponses.Error("Database error occurred"))
            }
        }

        notesDataRef.addListenerForSingleValueEvent(listener)
        organizersDataRef.addListenerForSingleValueEvent(listener)
    }
}