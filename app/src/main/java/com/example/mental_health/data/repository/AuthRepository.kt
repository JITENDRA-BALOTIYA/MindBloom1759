package com.example.mental_health.data.repository

import com.example.mental_health.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseFirestore,
    private val firestore: FirebaseFirestore
) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db           = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    // ─── Register ─────────────────────────────────────────────────────────────
    // Saves ALL student fields to Firestore → "users" collection
    suspend fun registerUser(
        name: String,
        age: Int,
        gender: String,
        collegeName: String,
        course: String,
        year: String,
        rollNumber: String,
        parentName: String,
        parentPhone: String,
        emergencyContact: String,
        email: String,
        password: String
    ): Result<Unit> = runCatching {

        // Step 1: Create Firebase Auth account
        val authResult = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val uid = authResult.user?.uid
            ?: throw Exception("Registration failed. Please try again.")

        // Step 2: Save student data to Firestore → users/{uid}
        val userDoc = mapOf(
            "uid"              to uid,
            "name"             to name,
            "email"            to email,
            "age"              to age,
            "gender"           to gender,
            "collegeName"      to collegeName,
            "course"           to course,
            "year"             to year,
            "rollNumber"       to rollNumber,
            "parentName"       to parentName,
            "parentPhone"      to parentPhone,
            "emergencyContact" to emergencyContact,
            "profileImage"     to "",
            "role"             to "student",
            "createdAt"        to System.currentTimeMillis()
        )

        db.collection("users")   // ← consistent "users" collection
            .document(uid)
            .set(userDoc)
            .await()
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        Unit
    }

    // ─── Get User Details ─────────────────────────────────────────────────────
    suspend fun getUserDetails(uid: String): Result<User> = runCatching {
        val doc = db.collection("users")   // ← consistent "users" collection
            .document(uid)
            .get()
            .await()

        doc.toObject(User::class.java)
            ?: throw Exception("User data not found.")
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    fun logout() {
        firebaseAuth.signOut()
    }
}