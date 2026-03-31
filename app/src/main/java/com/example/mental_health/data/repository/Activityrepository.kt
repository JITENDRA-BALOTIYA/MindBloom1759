package com.example.mental_health.data.repository



import com.example.mental_health.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Current user's UID ────────────────────────────────────────────────────
    private val uid get() = auth.currentUser?.uid ?: error("User not logged in")

    // ── Firestore paths ───────────────────────────────────────────────────────
    private fun studentDoc(studentId: String = uid) =
        firestore.collection("students").document(studentId)

    private fun activitiesCol(studentId: String = uid) =
        studentDoc(studentId).collection("activities")

    private fun reportsCol(studentId: String = uid) =
        studentDoc(studentId).collection("weeklyReports")

    // ─────────────────────────────────────────────────────────────────────────
    //  LOG ACTIVITY  (called from each feature screen)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun logAttendance(log: AttendanceLog) {
        activitiesCol()
            .document("attendance_${log.date}_${log.subject.replace(" ", "_")}")
            .set(mapOf(
                "type"      to "attendance",
                "date"      to log.date,
                "status"    to log.status,
                "subject"   to log.subject,
                "timestamp" to log.timestamp
            ))
            .await()
    }

    suspend fun logStress(log: StressLog) {
        activitiesCol()
            .add(mapOf(
                "type"      to "stress",
                "level"     to log.level,
                "note"      to log.note,
                "timestamp" to log.timestamp
            ))
            .await()
    }

    suspend fun logMeditation(log: MeditationLog) {
        activitiesCol()
            .add(mapOf(
                "type"            to "meditation",
                "durationMinutes" to log.durationMinutes,
                "sessionType"     to log.sessionType,
                "timestamp"       to log.timestamp
            ))
            .await()
    }

    suspend fun logAiChat(log: AiChatLog) {
        activitiesCol()
            .add(mapOf(
                "type"         to "ai_chat",
                "messageCount" to log.messageCount,
                "topicTag"     to log.topicTag,
                "timestamp"    to log.timestamp
            ))
            .await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FETCH ACTIVITIES for a given week  (used by ReportGenerator)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getActivitiesForWeek(
        studentId: String,
        weekStart: Date,
        weekEnd: Date
    ): List<Map<String, Any>> {
        val startTs = Timestamp(weekStart)
        val endTs   = Timestamp(weekEnd)

        return activitiesCol(studentId)
            .whereGreaterThanOrEqualTo("timestamp", startTs)
            .whereLessThanOrEqualTo("timestamp", endTs)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.data }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SAVE WEEKLY REPORT
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveWeeklyReport(report: WeeklyReport) {
        val reportMap = mapOf(
            "reportId"               to report.reportId,
            "studentId"              to report.studentId,
            "studentName"            to report.studentName,
            "weekStartDate"          to report.weekStartDate,
            "weekEndDate"            to report.weekEndDate,
            "generatedAt"            to report.generatedAt,
            "totalClasses"           to report.totalClasses,
            "presentDays"            to report.presentDays,
            "absentDays"             to report.absentDays,
            "lateDays"               to report.lateDays,
            "attendancePercent"      to report.attendancePercent,
            "avgStressLevel"         to report.avgStressLevel,
            "maxStressLevel"         to report.maxStressLevel,
            "stressCheckInCount"     to report.stressCheckInCount,
            "stressRiskFlag"         to report.stressRiskFlag,
            "totalMeditationMinutes" to report.totalMeditationMinutes,
            "meditationSessionCount" to report.meditationSessionCount,
            "totalAiMessages"        to report.totalAiMessages,
            "aiChatSessionCount"     to report.aiChatSessionCount,
            "dominantTopic"          to report.dominantTopic,
            "wellnessScore"          to report.wellnessScore,
            "adminNote"              to report.adminNote
        )

        // Save under student's own sub-collection
        reportsCol(report.studentId)
            .document(report.reportId)
            .set(reportMap)
            .await()

        // Also save to top-level admin collection for easy dashboard queries
        firestore
            .collection("admin")
            .document("allReports")
            .collection("reports")
            .document(report.reportId)
            .set(reportMap)
            .await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FETCH REPORTS  (for Admin Panel)
    // ─────────────────────────────────────────────────────────────────────────

    /** Real-time stream of ALL student reports (admin panel use) */
    fun getAllReportsFlow(): Flow<List<WeeklyReport>> = callbackFlow {
        val listener = firestore
            .collection("admin")
            .document("allReports")
            .collection("reports")
            .orderBy("generatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.toWeeklyReport()
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    /** One student's reports (for in-app student history) */
    fun getStudentReportsFlow(studentId: String = uid): Flow<List<WeeklyReport>> = callbackFlow {
        val listener = reportsCol(studentId)
            .orderBy("generatedAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val reports = snapshot?.documents?.mapNotNull { it.data?.toWeeklyReport() } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    /** Admin can update their note on any report */
    suspend fun updateAdminNote(reportId: String, studentId: String, note: String) {
        val update = mapOf("adminNote" to note)
        reportsCol(studentId).document(reportId).update(update).await()
        firestore.collection("admin").document("allReports")
            .collection("reports").document(reportId).update(update).await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FETCH ALL STUDENTS  (for admin dropdown)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getAllStudents(): List<Map<String, String>> {
        return firestore.collection("students")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapOf(
                    "uid"   to (doc.id),
                    "name"  to (data["name"] as? String ?: "Unknown"),
                    "email" to (data["email"] as? String ?: "")
                )
            }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Extension: Map → WeeklyReport
// ─────────────────────────────────────────────────────────────────────────────
private fun Map<String, Any>.toWeeklyReport() = WeeklyReport(
    reportId               = this["reportId"] as? String ?: "",
    studentId              = this["studentId"] as? String ?: "",
    studentName            = this["studentName"] as? String ?: "",
    weekStartDate          = this["weekStartDate"] as? String ?: "",
    weekEndDate            = this["weekEndDate"] as? String ?: "",
    generatedAt            = this["generatedAt"] as? Timestamp ?: Timestamp.now(),
    totalClasses           = (this["totalClasses"] as? Long)?.toInt() ?: 0,
    presentDays            = (this["presentDays"] as? Long)?.toInt() ?: 0,
    absentDays             = (this["absentDays"] as? Long)?.toInt() ?: 0,
    lateDays               = (this["lateDays"] as? Long)?.toInt() ?: 0,
    attendancePercent      = (this["attendancePercent"] as? Double)?.toFloat() ?: 0f,
    avgStressLevel         = (this["avgStressLevel"] as? Double)?.toFloat() ?: 0f,
    maxStressLevel         = (this["maxStressLevel"] as? Long)?.toInt() ?: 0,
    stressCheckInCount     = (this["stressCheckInCount"] as? Long)?.toInt() ?: 0,
    stressRiskFlag         = this["stressRiskFlag"] as? Boolean ?: false,
    totalMeditationMinutes = (this["totalMeditationMinutes"] as? Long)?.toInt() ?: 0,
    meditationSessionCount = (this["meditationSessionCount"] as? Long)?.toInt() ?: 0,
    totalAiMessages        = (this["totalAiMessages"] as? Long)?.toInt() ?: 0,
    aiChatSessionCount     = (this["aiChatSessionCount"] as? Long)?.toInt() ?: 0,
    dominantTopic          = this["dominantTopic"] as? String ?: "",
    wellnessScore          = (this["wellnessScore"] as? Long)?.toInt() ?: 0,
    adminNote              = this["adminNote"] as? String ?: ""
)