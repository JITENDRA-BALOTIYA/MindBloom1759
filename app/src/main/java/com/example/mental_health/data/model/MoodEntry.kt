package com.example.mental_health.data.model

import com.google.firebase.Timestamp

data class MoodEntry(
    val uid: String = "",
    val mood: String = "", // Happy, Neutral, Sad, Stressed, Depressed
    val timestamp: Timestamp = Timestamp.now(),
    val note: String = "",
    val stressScore: Int = 0
)
