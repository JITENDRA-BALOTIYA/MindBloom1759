package com.example.mental_health.data.model



import com.google.firebase.Timestamp

// ─────────────────────────────────────────────────────────────────────────────
//  Activity Models  (student ke individual actions)
// ─────────────────────────────────────────────────────────────────────────────

data class AttendanceLog(
    val date: String = "",          // "2024-03-11"
    val status: String = "present", // "present" | "absent" | "late"
    val subject: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class StressLog(
    val level: Int = 0,             // 1–10 scale
    val note: String = "",          // optional student note
    val timestamp: Timestamp = Timestamp.now()
)

data class MeditationLog(
    val durationMinutes: Int = 0,
    val sessionType: String = "",   // "breathing" | "guided" | "music"
    val timestamp: Timestamp = Timestamp.now()
)

data class AiChatLog(
    val messageCount: Int = 0,
    val topicTag: String = "",      // "stress" | "study" | "general"
    val timestamp: Timestamp = Timestamp.now()
)

// ─────────────────────────────────────────────────────────────────────────────
//  Weekly Report  (generated every Sunday, stored in Firestore)
// ─────────────────────────────────────────────────────────────────────────────

data class WeeklyReport(
    val reportId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val weekStartDate: String = "",     // "2024-03-11"
    val weekEndDate: String = "",       // "2024-03-17"
    val generatedAt: Timestamp = Timestamp.now(),

    // Attendance
    val totalClasses: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val attendancePercent: Float = 0f,  // 0–100

    // Stress
    val avgStressLevel: Float = 0f,     // 1–10
    val maxStressLevel: Int = 0,
    val stressCheckInCount: Int = 0,
    val stressRiskFlag: Boolean = false, // true if avg > 7

    // Meditation
    val totalMeditationMinutes: Int = 0,
    val meditationSessionCount: Int = 0,

    // AI Chat
    val totalAiMessages: Int = 0,
    val aiChatSessionCount: Int = 0,
    val dominantTopic: String = "",     // most frequent topic tag

    // Overall wellness score (0–100, computed)
    val wellnessScore: Int = 0,

    // Admin note (editable from dashboard)
    val adminNote: String = ""
)

// ─────────────────────────────────────────────────────────────────────────────
//  Helper: Risk Level based on report data
// ─────────────────────────────────────────────────────────────────────────────

enum class RiskLevel { LOW, MEDIUM, HIGH }

fun WeeklyReport.riskLevel(): RiskLevel = when {
    stressRiskFlag && attendancePercent < 60f -> RiskLevel.HIGH
    stressRiskFlag || attendancePercent < 75f -> RiskLevel.MEDIUM
    else                                       -> RiskLevel.LOW
}