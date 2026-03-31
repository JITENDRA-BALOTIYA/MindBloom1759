@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mental_health.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.ui.navigation.Screen
import com.example.mental_health.ui.viewmodel.AuthViewModel
import com.example.mental_health.ui.viewmodel.MeditationViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// ─── Apple Style Color System ───────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentGreen  = Color(0xFF34C759)
private val AccentCoral  = Color(0xFFFF3B30)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentAmber  = Color(0xFFFF9F0A)
private val AccentBlue   = Color(0xFF007AFF)
private val AccentSlate  = Color(0xFF8E8E93)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)

// ─── Data Models ───────────────────────────────────────────────────────────
data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color,
    val bgColor: Color
)

data class RelaxItem(
    val id: String,
    val title: String,
    val duration: String,
    val emoji: String,
    val iconBg: Color,
    val type: RelaxType,
    val sessionId: String? = null
)

enum class RelaxType {
    BREATHING,
    GUIDED_MEDITATION,
    SLEEP_STORY,
    MUSIC,
    FOCUS
}

// ─── Main Dashboard Screen ─────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    meditationViewModel: MeditationViewModel = hiltViewModel()
) {
    val user by authViewModel.currentUser.collectAsState()

    val features = remember {
        listOf(
            FeatureItem("AI Assistant",  "Mental health guide",  Icons.Default.Chat,             Screen.AIChat.route,            AccentIndigo, Color(0xFFEEF0FF)),
            FeatureItem("Stress Test",   "Quick assessment",     Icons.Default.Assignment,        Screen.PsychologyTest.route,    AccentCoral,  Color(0xFFFFECEE)),
            FeatureItem("Meditation",    "Calm your soul",       Icons.Default.SelfImprovement,   Screen.Meditation.route,        AccentGreen,  Color(0xFFE8FFF4)),
            FeatureItem("Relax Mode",    "Unwind & Destress",    Icons.Default.AutoAwesome,       Screen.RelaxMode.route,         AccentPurple, Color(0xFFF3EEFF)),
            FeatureItem("Activity",      "Wellness journey",     Icons.Default.BarChart,          Screen.AttendanceHistory.route, AccentAmber,  Color(0xFFFFF8E8)),
            FeatureItem("Settings",      "App preferences",      Icons.Default.Settings,          Screen.Settings.route,          AccentSlate,  Color(0xFFF0F0F5))
        )
    }

    val chunkedFeatures = remember(features) { features.chunked(2) }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        InfiniteFloatingOrbs()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                AppleHeader(
                    userName = user?.name ?: "Friend",
                    onProfileClick = { onNavigate(Screen.Profile.route) }
                )
            }

            item {
                MoodHeroCard(onCheckMood = { onNavigate(Screen.MoodCheck.route) })
            }

            item { Spacer(Modifier.height(14.dp)) }
            item { QuickStatsRow() }
            item { Spacer(Modifier.height(14.dp)) }

            item {
                AppleAttendanceCard(
                    onMarkAttendance = { onNavigate(Screen.FaceAttendance.route) },
                    onViewHistory    = { onNavigate(Screen.AttendanceHistory.route) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                AppleRelaxSection(
                    meditationViewModel  = meditationViewModel,
                    onNavigateToMeditation = { onNavigate(Screen.Meditation.route) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                AppleSectionHeader(title = "Explore", actionLabel = "See All", onAction = {})
            }
            item { Spacer(Modifier.height(12.dp)) }

            item {
                AppleFeaturesGrid(chunkedFeatures = chunkedFeatures, onNavigate = onNavigate)
            }

            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

// ─── Header ────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppleHeader(userName: String, onProfileClick: () -> Unit) {
    val today   = remember { LocalDate.now() }
    val dayStr  = remember(today) { today.dayOfWeek.getDisplayName(TextStyle.FULL,  Locale.ENGLISH).uppercase() }
    val dateStr = remember(today) { "${today.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${today.dayOfMonth}" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text("$dayStr, $dateStr", fontSize = 13.sp, fontWeight = FontWeight.W600, color = Label2, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text("Good Morning,\n$userName 🌿", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Label1, lineHeight = 32.sp)
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape, spotColor = Color.Black.copy(0.08f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFE3F0FF), Color(0xFFD1E8FF))))
                .border(2.dp, BgCard, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.size(22.dp))
        }
    }
}

// ─── Mood Hero Card ────────────────────────────────────────────────────────
@Composable
fun MoodHeroCard(onCheckMood: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label        = "moodCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = AccentIndigo.copy(0.25f))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF5E5CE6), Color(0xFF7C7CFF), Color(0xFFAF52DE))
                )
            )
            .clickable(interactionSource = interactionSource, indication = null) { onCheckMood() }
            .padding(24.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("How are you feeling?", fontSize = 13.sp, color = Color.White.copy(0.75f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text("Check your mood", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(0.2f))
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text("Start Check-in →", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Text("😊", fontSize = 56.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

// ─── Quick Stats Row ───────────────────────────────────────────────────────
@Composable
fun QuickStatsRow() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppleStatCard(
            modifier    = Modifier.weight(1f),
            label       = "Streak",
            value       = "7",
            unit        = "days",
            icon        = Icons.Default.LocalFireDepartment,
            iconColor   = AccentAmber,
            iconBg      = Color(0xFFFFF8E8)
        )
        AppleStatCard(
            modifier    = Modifier.weight(1f),
            label       = "Sessions",
            value       = "12",
            unit        = "this week",
            icon        = Icons.Default.SelfImprovement,
            iconColor   = AccentGreen,
            iconBg      = Color(0xFFE8FFF4)
        )
        AppleStatCard(
            modifier    = Modifier.weight(1f),
            label       = "Score",
            value       = "84",
            unit        = "/ 100",
            icon        = Icons.Default.Favorite,
            iconColor   = AccentCoral,
            iconBg      = Color(0xFFFFECEE)
        )
    }
}

// ─── Stat Card ─────────────────────────────────────────────────────────────
@Composable
fun AppleStatCard(
    modifier:   Modifier,
    label:      String,
    value:      String,
    unit:       String,
    icon:       ImageVector,
    iconColor:  Color,
    iconBg:     Color
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.05f))
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Label1)
            Text(unit,  fontSize = 10.sp, color = Label2, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Label2)
        }
    }
}

// ─── Attendance Card ───────────────────────────────────────────────────────
@Composable
fun AppleAttendanceCard(
    onMarkAttendance: () -> Unit,
    onViewHistory:    () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.06f))
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8FFF4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Label1)
                    Text("Mark & track your presence", fontSize = 12.sp, color = Label2)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mark Attendance button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentGreen)
                        .clickable { onMarkAttendance() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mark Present", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                // View History button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF2F2F7))
                        .clickable { onViewHistory() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("View History", fontSize = 13.sp, color = Label1, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Relax Section (NO TTS / voice) ───────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppleRelaxSection(
    meditationViewModel:    MeditationViewModel,
    onNavigateToMeditation: () -> Unit
) {
    val uiState       by meditationViewModel.uiState.collectAsState()
    val activeSession by meditationViewModel.activeSession.collectAsState()

    val relaxItems = remember(uiState.sessions) {
        listOf(
            RelaxItem(
                id        = "deep_breathing",
                title     = "Deep Breathing",
                duration  = "5 min",
                emoji     = "🌬️",
                iconBg    = Color(0xFFE8FFF4),
                type      = RelaxType.BREATHING,
                sessionId = uiState.sessions.find {
                    it.title.contains("Deep", ignoreCase = true) ||
                            it.title.contains("Breathing", ignoreCase = true)
                }?.id
            ),
            RelaxItem(
                id        = "guided_focus",
                title     = "Guided Focus",
                duration  = "10 min",
                emoji     = "🧘",
                iconBg    = Color(0xFFEEF0FF),
                type      = RelaxType.GUIDED_MEDITATION,
                sessionId = uiState.sessions.find {
                    it.title.contains("Mindful", ignoreCase = true) ||
                            it.title.contains("Focus", ignoreCase = true)
                }?.id
            ),
            RelaxItem(
                id        = "sleep_story",
                title     = "Sleep Story",
                duration  = "15 min",
                emoji     = "🌙",
                iconBg    = Color(0xFFFFF3E8),
                type      = RelaxType.SLEEP_STORY,
                sessionId = null
            )
        )
    }

    AppleSectionHeader(
        title       = "Quick Relaxation",
        actionLabel = "See All",
        onAction    = onNavigateToMeditation
    )

    Spacer(Modifier.height(12.dp))

    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(relaxItems, key = { it.id }) { item ->
            AppleRelaxCard(
                item     = item,
                isActive = activeSession.session?.id == item.sessionId,
                // ── NO TTS ──────────────────────────────────────────────
                onClick  = {
                    val session = uiState.sessions.find { it.id == item.sessionId }
                    if (session != null &&
                        (item.type == RelaxType.BREATHING || item.type == RelaxType.GUIDED_MEDITATION)
                    ) {
                        meditationViewModel.startSession(session)   // just start, no voice
                    } else {
                        onNavigateToMeditation()                    // navigate, no voice
                    }
                }
            )
        }
    }
}

// ─── Relax Card (NO TTS) ──────────────────────────────────────────────────
@Composable
fun AppleRelaxCard(
    item:     RelaxItem,
    isActive: Boolean = false,
    onClick:  () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.93f else if (isActive) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "cardScale"
    )

    // Fix: correct animated float per type, no 'by' on 'when'
    val breathingAnim by animateFloatAsState(
        targetValue   = if (isActive && item.type == RelaxType.BREATHING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label         = "breathingAnim"
    )
    val meditationAnim by animateFloatAsState(
        targetValue   = if (isActive && item.type == RelaxType.GUIDED_MEDITATION) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label         = "meditationAnim"
    )
    val sleepAnim by animateFloatAsState(
        targetValue   = if (isActive && item.type == RelaxType.SLEEP_STORY) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label         = "sleepAnim"
    )

    val emojiScale = when (item.type) {
        RelaxType.BREATHING         -> breathingAnim
        RelaxType.GUIDED_MEDITATION -> meditationAnim
        RelaxType.SLEEP_STORY       -> sleepAnim
        else                        -> 1f
    }

    Box(
        modifier = Modifier
            .width(152.dp)
            .scale(cardScale)
            .shadow(
                elevation  = if (isActive) 16.dp else 7.dp,
                shape      = RoundedCornerShape(26.dp),
                spotColor  = if (isActive) AccentGreen.copy(0.4f) else Color.Black.copy(0.08f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(BgCard)
            .border(
                width = if (isActive) 2.5.dp else 0.dp,
                color = if (isActive) AccentGreen else Color.Transparent,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(emojiScale)
                    .clip(RoundedCornerShape(18.dp))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 28.sp)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text     = item.title,
                fontWeight = FontWeight.Bold,
                color    = Label1,
                fontSize = 14.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = item.duration,
                fontSize   = 12.5.sp,
                color      = if (isActive) AccentGreen else Label2,
                fontWeight = FontWeight.Medium
            )

            if (isActive) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(AccentGreen, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text("Now Playing", fontSize = 10.5.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Features Grid ────────────────────────────────────────────────────────
@Composable
fun AppleFeaturesGrid(chunkedFeatures: List<List<FeatureItem>>, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        chunkedFeatures.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    AppleFeatureCard(item = item, onNavigate = onNavigate, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AppleFeatureCard(item: FeatureItem, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "featureCardScale"
    )

    Box(
        modifier = modifier
            .height(136.dp)
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(22.dp))
            .background(BgCard)
            .clickable(interactionSource = interactionSource, indication = null) { onNavigate(item.route) }
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(item.title,       fontWeight = FontWeight.Bold,   color = Label1, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(item.description, fontSize   = 11.sp, color = Label2, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ─── Section Header ────────────────────────────────────────────────────────
@Composable
fun AppleSectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title,       fontSize = 18.sp, fontWeight = FontWeight.Bold,    color = Label1)
        Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AccentIndigo,
            modifier = Modifier.clickable { onAction() })
    }
}

// ─── Floating Orbs Background ──────────────────────────────────────────────
@Composable
fun InfiniteFloatingOrbs() {
    val transition = rememberInfiniteTransition(label = "orbs")

    val yOffset1 by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 50f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label         = "orb1"
    )
    val yOffset2 by transition.animateFloat(
        initialValue  = 100f,
        targetValue   = -50f,
        animationSpec = infiniteRepeatable(tween(20000, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label         = "orb2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color  = Color(0xFF6366F1).copy(alpha = 0.04f),
            radius = 350f,
            center = center.copy(x = size.width * 0.1f, y = center.y + yOffset1)
        )
        drawCircle(
            color  = Color(0xFF10B981).copy(alpha = 0.04f),
            radius = 450f,
            center = center.copy(x = size.width * 0.9f, y = center.y + yOffset2)
        )
    }
}