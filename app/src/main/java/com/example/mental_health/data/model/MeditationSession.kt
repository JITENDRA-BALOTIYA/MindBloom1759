package com.example.mental_health.data.model



data class MeditationSession(
    val id: String              = "",
    val title: String           = "",
    val subtitle: String        = "",
    val description: String     = "",
    val emoji: String           = "",
    val durationMinutes: Int    = 5,
    val level: String           = "Beginner",
    val category: String        = "Breathing",
    val gradientStart: String   = "#5E5CE6",
    val gradientEnd: String     = "#7C7CFF",
    val levelColorHex: String   = "#34C759",
    val completedCount: Int     = 0
)

data class CompletedSession(
    val sessionId: String       = "",
    val sessionTitle: String    = "",
    val durationMinutes: Int    = 0,
    val completedAt: Long       = System.currentTimeMillis(),
    val userId: String          = ""
)