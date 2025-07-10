package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.SyllabusModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.HOME
import com.subhajitrajak.makautstudybuddy.utils.Constants.SYLLABUS_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class SyllabusRepo(val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.getReference(SYLLABUS_DATA).child(HOME)
    private val syllabusLD = MutableLiveData<MyResponses<ArrayList<SyllabusModel>>>()

    val syllabusLiveData get() = syllabusLD

    fun getSyllabusData() {
        syllabusLiveData.postValue(MyResponses.Loading())
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                if (!snapshots.exists()) {
                    syllabusLiveData.postValue(MyResponses.Error("No Data Found"))
                    return
                }
                val tempList = ArrayList<SyllabusModel>()
                for (snapshot in snapshots.children) {
                    val syllabusModel = snapshot.getValue(SyllabusModel::class.java)
                    syllabusModel?.let {
                        tempList.add(syllabusModel)
                    }
                }
                if(tempList.isNotEmpty())
                    syllabusLiveData.postValue(MyResponses.Success(tempList))
            }

            override fun onCancelled(error: DatabaseError) {
                syllabusLiveData.postValue(MyResponses.Error("Something went wrong with Database."))
            }
        })
    }
}

