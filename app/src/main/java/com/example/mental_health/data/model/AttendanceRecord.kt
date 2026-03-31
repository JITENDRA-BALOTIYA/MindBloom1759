package com.example.mental_health.data.model

data class AttendanceRecord(
    val date: String = "",
    val time: String = "",
    val status: String = "Present",
    val faceVerified: Boolean = true
)
