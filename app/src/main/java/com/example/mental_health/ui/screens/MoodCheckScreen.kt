package com.example.mental_health.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.ui.components.CameraPreview
import com.example.mental_health.ui.viewmodel.MoodViewModel
import com.google.firebase.auth.FirebaseAuth

// ─── Design tokens ────────────────────────────────────────────────────────────
private val BgDeep        = Color(0xFF080B12)
private val BgGlass       = Color(0xFF1A2030)
private val AccentMint    = Color(0xFF00E5C3)
private val AccentPurple  = Color(0xFF9B6FFF)
private val AccentCoral   = Color(0xFFFF6B8A)
private val AccentAmber   = Color(0xFFFFB830)
private val AccentBlue    = Color(0xFF4D9FFF)
private val TextPrimary   = Color(0xFFF0F4FF)
private val TextSecondary = Color(0xFF8892A4)
private val DangerRed     = Color(0xFFFF4D6A)

data class MoodData(
    val emotion: String,
    val emoji: String,
    val color: Color,
    val description: String,
    val recommendation: String
)

private val moods = mapOf(
    "Joyful" to MoodData(
        "Joyful", "😄", AccentMint,
        "You seem to be in a wonderful mood!",
        "Keep this positive energy going. Maybe share it with a friend?"
    ),
    "Calm" to MoodData(
        "Calm", "😌", AccentBlue,
        "You appear very peaceful and centered.",
        "A perfect time for some light reading or a short walk."
    ),
    "Sad" to MoodData(
        "Sad", "😔", AccentPurple,
        "You look a bit down or thoughtful.",
        "It's okay to feel this way. A short meditation might help clear your mind."
    ),
    "Stressed" to MoodData(
        "Stressed", "🤯", DangerRed,
        "You seem to be carrying some tension.",
        "Try a 5-minute deep breathing exercise to release the pressure."
    ),
    "Anxious" to MoodData(
        "Anxious", "😰", AccentAmber,
        "You look a little worried or restless.",
        "Focus on the present moment. Try the 5-4-3-2-1 grounding technique."
    ),
    "Neutral" to MoodData(
        "Neutral", "😐", TextSecondary,
        "You have a balanced, steady expression.",
        "Check in with yourself. How are you really feeling inside?"
    )
)

@Composable
fun MoodCheckScreen(
    onBack: () -> Unit,
    onNavigateToMeditation: () -> Unit,
    viewModel: MoodViewModel = hiltViewModel()
) {
    var detectedMood by remember { mutableStateOf<String?>(null) }
    var showResult   by remember { mutableStateOf(false) }
    var moodSaved    by remember { mutableStateOf(false) }

    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val isSaving    by viewModel.isSaving.collectAsState()

    // Auto-save to Firebase when mood is detected
    LaunchedEffect(detectedMood) {
        val mood = detectedMood ?: return@LaunchedEffect
        if (!moodSaved) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            // stressScore: map mood → numeric score for weekly report
            val stressScore = when (mood) {
                "Stressed" -> 8
                "Anxious"  -> 7
                "Sad"      -> 6
                "Neutral"  -> 4
                "Calm"     -> 2
                "Joyful"   -> 1
                else       -> 4
            }
            viewModel.saveMood(uid = uid, mood = mood, note = "Auto-detected via face scan", stressScore = stressScore)
            moodSaved = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {

        if (!showResult) {
            // ── Camera / Scanning View ────────────────────────────────────────
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onMoodDetected = { mood ->
                    detectedMood = mood
                    showResult = true
                }
            )

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            // Bottom Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Scanning Emotion...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Align your face within the frame",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

        } else {
            // ── Result View ───────────────────────────────────────────────────
            val moodInfo = moods[detectedMood] ?: moods["Neutral"]!!

            MoodResultContent(
                moodInfo    = moodInfo,
                isSaving    = isSaving,
                saveSuccess = saveSuccess,
                onRetake    = {
                    showResult = false
                    moodSaved  = false
                    detectedMood = null
                    viewModel.resetSuccess()
                },
                onAction    = onNavigateToMeditation
            )
        }
    }
}

@Composable
private fun MoodResultContent(
    moodInfo: MoodData,
    isSaving: Boolean,
    saveSuccess: Boolean,
    onRetake: () -> Unit,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji & Glow
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(40.dp, CircleShape, spotColor = moodInfo.color)
                    .background(moodInfo.color.copy(alpha = 0.15f), CircleShape)
            )
            Text(moodInfo.emoji, fontSize = 80.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Current Mood: ${moodInfo.emotion}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        // Firebase save status indicator
        when {
            isSaving    -> Text("Saving mood...", fontSize = 12.sp, color = TextSecondary)
            saveSuccess -> Text("✓ Mood saved to your profile", fontSize = 12.sp, color = AccentMint)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            moodInfo.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Recommendation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgGlass)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Recommendation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = moodInfo.color,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    moodInfo.recommendation,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = moodInfo.color),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Try Meditation", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onRetake) {
            Text("Retake Scan", color = TextSecondary)
        }
    }
}
