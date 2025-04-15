package com.subhajitrajak.makautstudybuddy.data.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.VideosModel
import com.subhajitrajak.makautstudybuddy.utils.Constants.VIDEOS_DATA
import com.subhajitrajak.makautstudybuddy.utils.MyResponses

class VideosRepo (val context: Context) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef = firebaseDatabase.getReference(VIDEOS_DATA)
    private val videosLD = MutableLiveData<MyResponses<ArrayList<VideosModel>>>()

    val videosLiveData get() = videosLD

    suspend fun getVideosData() {
        videosLiveData.postValue(MyResponses.Loading())
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                if (!snapshots.exists()) {
                    videosLiveData.postValue(MyResponses.Error("No Data Found"))
                    return
                }
                val tempList = ArrayList<VideosModel>()
                for (snapshot in snapshots.children) {
                    val videosModel = snapshot.getValue(VideosModel::class.java)
                    videosModel?.let {
                        tempList.add(videosModel)
                    }
                }
                if(tempList.size>0)
                    videosLiveData.postValue(MyResponses.Success(tempList))
            }

            override fun onCancelled(error: DatabaseError) {
                videosLiveData.postValue(MyResponses.Error("Something went wrong with Database."))
            }
        })
    }
}