package com.example.mental_health.data.repository

import com.example.mental_health.data.model.MoodEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveMood(moodEntry: MoodEntry): Result<Unit> = try {
        firestore.collection("moods").add(moodEntry).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMoodHistory(uid: String): Result<List<MoodEntry>> = try {
        val snapshot = firestore.collection("moods")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
        val moods = snapshot.toObjects(MoodEntry::class.java)
        Result.success(moods)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
