package com.example.mental_health.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",

    // Academic
    val collegeName: String = "",       // single field, no duplicate
    val course: String = "",
    val year: String = "",
    val rollNumber: String = "",        // ← added

    // Emergency
    val parentName: String = "",
    val parentPhone: String = "",
    val emergencyContact: String = "",

    // Meta
    val profileImage: String = "",
    val createdAt: Long = 0L,
    val role: String = "student"
)