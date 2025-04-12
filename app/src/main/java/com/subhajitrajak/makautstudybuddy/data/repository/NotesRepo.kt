package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.HomeModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.NOTES_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class NotesRepo(val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.getReference("AppData").child(NOTES_DATA)
    private val notesLD = MutableLiveData<MyResponses<ArrayList<HomeModel>>>()

    val notesLiveData get() = notesLD

    suspend fun getNotesData() {
        notesLiveData.postValue(MyResponses.Loading())
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                if (!snapshots.exists()) {
                    notesLiveData.postValue(MyResponses.Error("No Data Found"))
                    return
                }
                val tempList = ArrayList<HomeModel>()
                for (snapshot in snapshots.children) {
                    val notesModel = snapshot.getValue(HomeModel::class.java)
                    notesModel?.let {
                        tempList.add(notesModel)
                    }
                }
                if(tempList.size>0)
                    notesLiveData.postValue(MyResponses.Success(tempList))
            }

            override fun onCancelled(error: DatabaseError) {
                notesLiveData.postValue(MyResponses.Error("Something went wrong with Database."))
            }

        })
    }

}