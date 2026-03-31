package com.example.mental_health.weeklyReport



import com.example.mental_health.data.model.WeeklyReport
import com.example.mental_health.data.repository.ActivityRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WeeklyReportGenerator @Inject constructor(
    private val repository: ActivityRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─────────────────────────────────────────────────────────────────────────
    //  Generate report for ONE student for the PAST week
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun generateForStudent(studentId: String): WeeklyReport {

        // 1. Get student name from Firestore
        val studentDoc = firestore.collection("students").document(studentId).get().await()
        val studentName = studentDoc.getString("name") ?: "Unknown"

        // 2. Compute week range (last Monday → last Sunday)
        val (weekStart, weekEnd) = getLastWeekRange()
        val weekStartStr = dateFormat.format(weekStart)
        val weekEndStr   = dateFormat.format(weekEnd)

        // 3. Fetch all activities for that week
        val activities = repository.getActivitiesForWeek(studentId, weekStart, weekEnd)

        // 4. Split by type
        val attendanceLogs  = activities.filter { it["type"] == "attendance" }
        val stressLogs      = activities.filter { it["type"] == "stress" }
        val meditationLogs  = activities.filter { it["type"] == "meditation" }
        val aiChatLogs      = activities.filter { it["type"] == "ai_chat" }

        // ── Attendance stats ──────────────────────────────────────────────────
        val totalClasses     = attendanceLogs.size
        val presentDays      = attendanceLogs.count { it["status"] == "present" }
        val absentDays       = attendanceLogs.count { it["status"] == "absent" }
        val lateDays         = attendanceLogs.count { it["status"] == "late" }
        val attendancePct    = if (totalClasses > 0)
            ((presentDays + lateDays * 0.5f) / totalClasses * 100f)
        else 0f

        // ── Stress stats ──────────────────────────────────────────────────────
        val stressLevels     = stressLogs.mapNotNull { (it["level"] as? Long)?.toInt() }
        val avgStress        = if (stressLevels.isNotEmpty()) stressLevels.average().toFloat() else 0f
        val maxStress        = stressLevels.maxOrNull() ?: 0
        val stressRisk       = avgStress > 7f

        // ── Meditation stats ──────────────────────────────────────────────────
        val totalMeditation  = meditationLogs
            .sumOf { (it["durationMinutes"] as? Long)?.toInt() ?: 0 }

        // ── AI Chat stats ─────────────────────────────────────────────────────
        val totalMessages    = aiChatLogs.sumOf { (it["messageCount"] as? Long)?.toInt() ?: 0 }
        val dominantTopic    = aiChatLogs
            .mapNotNull { it["topicTag"] as? String }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: ""

        // ── Wellness Score  (0–100) ───────────────────────────────────────────
        // Formula:
        //   attendance weight = 40%
        //   stress weight     = 30%  (inverted: low stress = high score)
        //   meditation weight = 20%
        //   AI chat weight    = 10%  (engagement bonus)
        val attendanceScore  = attendancePct.coerceIn(0f, 100f) * 0.40f
        val stressScore      = ((10f - avgStress.coerceIn(0f, 10f)) / 10f * 100f) * 0.30f
        val meditationScore  = (totalMeditation.coerceAtMost(60) / 60f * 100f) * 0.20f
        val chatScore        = (totalMessages.coerceAtMost(50) / 50f * 100f) * 0.10f
        val wellnessScore    = (attendanceScore + stressScore + meditationScore + chatScore).roundToInt()

        // 5. Build report object
        val reportId = "${studentId}_${weekStartStr}"

        return WeeklyReport(
            reportId               = reportId,
            studentId              = studentId,
            studentName            = studentName,
            weekStartDate          = weekStartStr,
            weekEndDate            = weekEndStr,
            generatedAt            = Timestamp.now(),
            totalClasses           = totalClasses,
            presentDays            = presentDays,
            absentDays             = absentDays,
            lateDays               = lateDays,
            attendancePercent      = attendancePct,
            avgStressLevel         = avgStress,
            maxStressLevel         = maxStress,
            stressCheckInCount     = stressLogs.size,
            stressRiskFlag         = stressRisk,
            totalMeditationMinutes = totalMeditation,
            meditationSessionCount = meditationLogs.size,
            totalAiMessages        = totalMessages,
            aiChatSessionCount     = aiChatLogs.size,
            dominantTopic          = dominantTopic,
            wellnessScore          = wellnessScore,
            adminNote              = ""
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Generate reports for ALL students (called from WorkManager)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun generateForAllStudents() {
        val students = repository.getAllStudents()
        students.forEach { student ->
            val uid = student["uid"] ?: return@forEach
            try {
                val report = generateForStudent(uid)
                repository.saveWeeklyReport(report)
            } catch (e: Exception) {
                // Log individual failure without stopping other students
                e.printStackTrace()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper: Get last Monday–Sunday range
    // ─────────────────────────────────────────────────────────────────────────

    private fun getLastWeekRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        // Go to last Sunday (end of week)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time

        // Go to last Monday (start of week)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val weekStart = calendar.time

        return Pair(weekStart, weekEnd)
    }
}