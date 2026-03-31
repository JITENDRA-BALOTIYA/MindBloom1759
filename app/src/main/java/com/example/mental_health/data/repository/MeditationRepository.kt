package com.example.mental_health.data.repository



import com.example.mental_health.data.model.CompletedSession
import com.example.mental_health.data.model.MeditationSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MeditationRepository @Inject constructor() {

    private val db   = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private val uid  get() = auth.currentUser?.uid ?: "anonymous"

    // ─────────────────────────────────────────────────────────────────────────
    //  SESSIONS — stored in Firebase under /meditation_sessions/
    //  We seed default sessions if none exist yet.
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getMeditationSessions(): Result<List<MeditationSession>> =
        suspendCancellableCoroutine { cont ->
            db.child("meditation_sessions")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = mutableListOf<MeditationSession>()
                    snapshot.children.forEach { child ->
                        val s = child.getValue(MeditationSession::class.java)
                        if (s != null) list.add(s.copy(id = child.key ?: ""))
                    }
                    // If Firebase has no sessions yet → seed defaults
                    if (list.isEmpty()) {
                        seedDefaultSessions()
                        cont.resume(Result.success(defaultSessions()))
                    } else {
                        cont.resume(Result.success(list))
                    }
                }
                .addOnFailureListener {
                    // Offline fallback → return hardcoded defaults
                    cont.resume(Result.success(defaultSessions()))
                }
        }

    // ─────────────────────────────────────────────────────────────────────────
    //  SAVE COMPLETED SESSION
    //  Path: /user_meditation/{uid}/{date}/{sessionId}
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun saveCompletedSession(
        sessionId: String,
        sessionTitle: String,
        durationMinutes: Int
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val today   = todayString()
        val record  = CompletedSession(
            sessionId       = sessionId,
            sessionTitle    = sessionTitle,
            durationMinutes = durationMinutes,
            completedAt     = System.currentTimeMillis(),
            userId          = uid
        )
        db.child("user_meditation").child(uid).child(today).child(sessionId)
            .setValue(record)
            .addOnSuccessListener { cont.resume(Result.success(Unit)) }
            .addOnFailureListener { cont.resume(Result.failure(it)) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STREAK  — count consecutive days that have at least 1 session
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getStreak(): Result<Int> =
        suspendCancellableCoroutine { cont ->
            db.child("user_meditation").child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val dates = snapshot.children
                        .mapNotNull { it.key }
                        .mapNotNull { runCatching { parseDate(it) }.getOrNull() }
                        .sortedDescending()

                    var streak  = 0
                    var current = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    for (date in dates) {
                        val diff = (current.time - date.time) / (1000 * 60 * 60 * 24)
                        if (diff <= 1) {
                            streak++
                            current = date
                        } else break
                    }
                    cont.resume(Result.success(streak))
                }
                .addOnFailureListener { cont.resume(Result.success(0)) }
        }

    // ─────────────────────────────────────────────────────────────────────────
    //  TOTAL MINUTES  — sum all completed session durations
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getTotalMinutes(): Result<Int> =
        suspendCancellableCoroutine { cont ->
            db.child("user_meditation").child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    var total = 0
                    snapshot.children.forEach { daySnap ->
                        daySnap.children.forEach { sessionSnap ->
                            val s = sessionSnap.getValue(CompletedSession::class.java)
                            total += s?.durationMinutes ?: 0
                        }
                    }
                    cont.resume(Result.success(total))
                }
                .addOnFailureListener { cont.resume(Result.success(0)) }
        }

    // ─────────────────────────────────────────────────────────────────────────
    //  COMPLETED TODAY
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getCompletedToday(): Result<Int> =
        suspendCancellableCoroutine { cont ->
            db.child("user_meditation").child(uid).child(todayString())
                .get()
                .addOnSuccessListener { cont.resume(Result.success(it.childrenCount.toInt())) }
                .addOnFailureListener { cont.resume(Result.success(0)) }
        }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun todayString() =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun parseDate(s: String): Date =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s)!!

    private fun seedDefaultSessions() {
        defaultSessions().forEach { session ->
            db.child("meditation_sessions").child(session.id).setValue(session)
        }
    }

    private fun defaultSessions() = listOf(
        MeditationSession(
            id              = "deep_breathing",
            title           = "Deep Breathing",
            subtitle        = "Focus on your breath. Inhale slowly...",
            description     = "Inhale slowly through your nose, hold for 4 seconds, then exhale gently. Repeat to calm your nervous system and reduce anxiety.",
            emoji           = "🌬️",
            durationMinutes = 5,
            level           = "Beginner",
            category        = "Breathing",
            gradientStart   = "#5E5CE6",
            gradientEnd     = "#7C7CFF",
            levelColorHex   = "#34C759"
        ),
        MeditationSession(
            id              = "box_breathing",
            title           = "Box Breathing",
            subtitle        = "Inhale 4s · Hold 4s · Exhale 4s",
            description     = "Inhale for 4s, hold for 4s, exhale for 4s, hold for 4s. Repeat this cycle to achieve deep relaxation.",
            emoji           = "📦",
            durationMinutes = 3,
            level           = "Intermediate",
            category        = "Breathing",
            gradientStart   = "#FF9F0A",
            gradientEnd     = "#FF6B00",
            levelColorHex   = "#FF9F0A"
        ),
        MeditationSession(
            id              = "neck_stretch",
            title           = "Neck Stretch",
            subtitle        = "Release muscle tension gently",
            description     = "Slowly tilt your head to each side, hold for 10 seconds, to release tension from your neck and shoulders.",
            emoji           = "🤸",
            durationMinutes = 2,
            level           = "Easy",
            category        = "Stretch",
            gradientStart   = "#007AFF",
            gradientEnd     = "#4D9FFF",
            levelColorHex   = "#007AFF"
        ),
        MeditationSession(
            id              = "eye_relaxation",
            title           = "Eye Relaxation",
            subtitle        = "Reduce eye strain with gentle movement",
            description     = "Close your eyes and move them in circles to reduce digital eye strain. Works great after screen time.",
            emoji           = "👁",
            durationMinutes = 1,
            level           = "Quick",
            category        = "Relaxation",
            gradientStart   = "#FF3B30",
            gradientEnd     = "#FF6B6B",
            levelColorHex   = "#FF3B30"
        ),
        MeditationSession(
            id              = "mindful_breathing",
            title           = "Mindful Breathing",
            subtitle        = "Observe each breath without changing it",
            description     = "Sit comfortably and observe each breath. Don't try to control it — just be aware. Let thoughts pass like clouds.",
            emoji           = "🧘",
            durationMinutes = 10,
            level           = "All levels",
            category        = "Mindfulness",
            gradientStart   = "#34C759",
            gradientEnd     = "#30D158",
            levelColorHex   = "#34C759"
        )
    )
}