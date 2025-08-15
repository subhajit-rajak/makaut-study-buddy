package com.subhajitrajak.makautstudybuddy.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.data.models.AiModel
import com.subhajitrajak.makautstudybuddy.utils.Constants
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AiModelsRepository {

    private val database = FirebaseDatabase.getInstance()
    private val aiModelsRef = database.getReference(Constants.AI_MODELS_DATA)

    suspend fun fetchAiModels(): List<AiModel> =
        suspendCancellableCoroutine { continuation ->
            aiModelsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val models = mutableListOf<AiModel>()

                    for (modelSnapshot in snapshot.children) {
                        val modelName = modelSnapshot.child("modelName").getValue(String::class.java) ?: ""
                        val identifier = modelSnapshot.child("identifier").getValue(String::class.java) ?: ""
                        val isPremium = modelSnapshot.child("isPremium").getValue(Boolean::class.java) ?: false
                        val isFaster = modelSnapshot.child("isFaster").getValue(Boolean::class.java) ?: false
                        val order = modelSnapshot.child("order").getValue(Int::class.java) ?: 0

                        val model = AiModel(
                            modelName = modelName,
                            identifier = identifier,
                            isPremium = isPremium,
                            isFaster = isFaster,
                            order = order
                        )

                        models.add(model)
                    }

                    continuation.resume(models.sortedBy { it.order })
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(
                        Exception("Failed to fetch AI models: ${error.message}")
                    )
                }
            })
        }
}