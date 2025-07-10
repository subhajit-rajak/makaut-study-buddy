package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.BooksModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.BOOKS_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class BookRepo(val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.getReference(BOOKS_DATA)
    private val bookLD = MutableLiveData<MyResponses<ArrayList<BooksModel>>>()

    val booksLiveData get() = bookLD

    fun getBooksData() {
        booksLiveData.postValue(MyResponses.Loading())
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                if (!snapshots.exists()) {
                    booksLiveData.postValue(MyResponses.Error("No Data Found"))
                    return
                }
                val tempList = ArrayList<BooksModel>()
                for (snapshot in snapshots.children) {
                    val booksModel = snapshot.getValue(BooksModel::class.java)
                    booksModel?.let {
                        tempList.add(booksModel)
                    }
                }
                if(tempList.isNotEmpty())
                    booksLiveData.postValue(MyResponses.Success(tempList))
            }

            override fun onCancelled(error: DatabaseError) {
                booksLiveData.postValue(MyResponses.Error("Something went wrong with Database."))
            }

        })
    }

}