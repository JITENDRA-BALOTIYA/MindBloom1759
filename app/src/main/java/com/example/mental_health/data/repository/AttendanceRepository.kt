package com.example.mental_health.data.repository

import com.example.mental_health.data.model.AttendanceRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    suspend fun markAttendance(): Result<Unit> {
        if (uid.isEmpty()) return Result.failure(Exception("User not logged in"))

        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)

        val record = AttendanceRecord(
            date = date,
            time = time,
            status = "Present",
            faceVerified = true
        )

        return try {
            db.getReference("attendance").child(uid).child(date).setValue(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendanceHistory(): Result<List<AttendanceRecord>> {
        if (uid.isEmpty()) return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = db.getReference("attendance").child(uid).get().await()
            val list = mutableListOf<AttendanceRecord>()
            snapshot.children.forEach {
                it.getValue(AttendanceRecord::class.java)?.let { record ->
                    list.add(record)
                }
            }
            Result.success(list.sortedByDescending { it.date })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
