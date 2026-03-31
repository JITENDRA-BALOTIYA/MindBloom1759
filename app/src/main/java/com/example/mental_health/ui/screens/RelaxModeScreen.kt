@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mental_health.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.example.mental_health.ui.viewmodel.RelaxViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Color System (same as before)
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentIndigoL= Color(0xFF7C7CFF)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentGreen  = Color(0xFF34C759)
private val AccentAmber  = Color(0xFFFF9F0A)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Label3       = Color(0xFFC7C7CC)
private val Separator    = Color(0xFFE5E5EA)

// ─────────────────────────────────────────────────────────────────────────────
//  Data Models (Breathing only — music/video now comes from ViewModel)
// ─────────────────────────────────────────────────────────────────────────────
data class BreathingExercise(
    val title: String,
    val duration: String,
    val emoji: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color
)

private val breathingExercises = listOf(
    BreathingExercise("Deep Calm", "5 mins", "🌬️", AccentIndigo, AccentIndigoL, AccentIndigo),
    BreathingExercise("Focus", "3 mins", "🎯", AccentGreen, Color(0xFF30D158), AccentGreen),
    BreathingExercise("Sleep Well", "10 mins", "🌙", AccentAmber, Color(0xFFFF6B00), AccentAmber),
    BreathingExercise("Body Scan", "7 mins", "✨", AccentPurple, Color(0xFFDA8FFF), AccentPurple),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RelaxModeScreen(
    onBack: () -> Unit,
    viewModel: RelaxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showVideoDialog by remember { mutableStateOf(false) }

    // Full-screen video dialog
    if (showVideoDialog && uiState.currentVideoIndex >= 0) {
        VideoPlayerDialog(
            player = viewModel.videoPlayer,
            title = uiState.videos[uiState.currentVideoIndex].title,
            onDismiss = {
                viewModel.stopVideo()
                showVideoDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Hero Header ────────────────────────────────────────────────
            item { RelaxHeroHeader(onBack = onBack) }

            // ── Breathing Exercises ────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                AppleSectionHeader(title = "Breathing Exercises", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(breathingExercises.size) { i ->
                        BreathingCard(exercise = breathingExercises[i])
                    }
                }
            }

            // ── Music Section ──────────────────────────────────────────────
            item {
                Spacer(Modifier.height(28.dp))
                AppleSectionHeader(title = "Motivational Music", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.musicTracks.forEachIndexed { index, track ->
                        MusicTrackCard(
                            title = track.title,
                            subtitle = track.subtitle,
                            duration = track.duration,
                            emoji = track.emoji,
                            isPlaying = uiState.currentTrackIndex == index && uiState.isMusicPlaying,
                            isSelected = uiState.currentTrackIndex == index,
                            progress = if (uiState.currentTrackIndex == index) uiState.musicProgress else 0f,
                            onClick = { viewModel.playTrack(index) },
                            onSeek = { viewModel.seekMusic(it) }
                        )
                    }
                }
            }

            // ── Meditation Videos ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(28.dp))
                AppleSectionHeader(title = "Meditation Videos 🎬", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.videos.size) { i ->
                        val video = uiState.videos[i]
                        VideoCard(
                            title = video.title,
                            description = video.description,
                            emoji = video.thumbnail,
                            duration = video.duration,
                            onClick = {
                                viewModel.playVideo(i)
                                showVideoDialog = true
                            }
                        )
                    }
                }
            }

            // ── Daily Quote ────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(28.dp))
                AppleSectionHeader(title = "Daily Quote ✨", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(14.dp))
                RelaxQuoteCard(
                    quote = "Your mental health is a priority. Your happiness is an essential. Your self-care is a necessity.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Hero Header (same as original)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RelaxHeroHeader(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orbScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(AccentIndigo, AccentPurple, AccentAmber),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 700f)
                )
            )
    ) {
        Box(Modifier.size(160.dp).offset(x = 240.dp, y = (-40).dp).background(Color.White.copy(0.07f), CircleShape))
        Box(Modifier.size(100.dp).offset(x = 290.dp, y = 30.dp).background(Color.White.copy(0.05f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp).clip(CircleShape)
                        .background(Color.White.copy(0.18f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.weight(1f))
                Text("Relax Mode", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(34.dp))
            }

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.size((90 * orbScale).dp).clip(CircleShape)
                    .background(Color.White.copy(0.15f))
                    .border(2.dp, Color.White.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("🧘", fontSize = 36.sp) }

            Spacer(Modifier.height(20.dp))
            Text("Breathe & Relax 🌿", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(6.dp))
            Text("Find peace in the present moment.", fontSize = 13.sp, color = Color.White.copy(0.75f))
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Section Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleSectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Label1)
        Text("See All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentIndigo)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Breathing Card (same as original)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BreathingCard(exercise: BreathingExercise) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = exercise.accentColor.copy(0.2f))
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .clickable { pressed = !pressed }
            .padding(16.dp)
            .width(120.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(exercise.gradientStart, exercise.gradientEnd))),
                contentAlignment = Alignment.Center
            ) { Text(exercise.emoji, fontSize = 20.sp) }

            Spacer(Modifier.height(10.dp))
            Text(exercise.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Label1)
            Text(exercise.duration, fontSize = 11.sp, color = Label2, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(exercise.accentColor.copy(alpha = 0.10f)).padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) { Text("▶ Start", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = exercise.accentColor) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Music Track Card — with real seekbar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MusicTrackCard(
    title: String,
    subtitle: String,
    duration: String,
    emoji: String,
    isPlaying: Boolean,
    isSelected: Boolean,
    progress: Float,
    onClick: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (isSelected) 6.dp else 2.dp,
                RoundedCornerShape(16.dp),
                spotColor = if (isSelected) AccentIndigo.copy(0.2f) else Color.Black.copy(0.05f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .then(
                if (isSelected) Modifier.border(1.5.dp, AccentIndigo.copy(0.20f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Track icon
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(AccentIndigo, AccentPurple))
                            else Brush.linearGradient(listOf(BgPrimary, BgPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 20.sp) }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold, color = Label1)
                    Text(subtitle, fontSize = 11.sp, color = Label2, modifier = Modifier.padding(top = 2.dp))
                }

                Text(duration, fontSize = 11.sp, color = Label2)

                // Play/Pause
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape)
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL))
                            else Brush.linearGradient(listOf(BgPrimary, BgPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPlaying) "⏸" else "▶",
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else AccentIndigo
                    )
                }
            }

            // Seekbar — only when selected
            AnimatedVisibility(visible = isSelected) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Slider(
                        value = progress,
                        onValueChange = onSeek,
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentIndigo,
                            activeTrackColor = AccentIndigo,
                            inactiveTrackColor = Separator
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Video Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun VideoCard(
    title: String,
    description: String,
    emoji: String,
    duration: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .clickable { onClick() }
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.linearGradient(listOf(AccentIndigo, AccentPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 40.sp)

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(duration, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Play overlay
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", fontSize = 16.sp, color = Color.White)
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Label1)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 11.sp, color = Label2, maxLines = 2)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Full-screen Video Player Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun VideoPlayerDialog(
    player: androidx.media3.exoplayer.ExoPlayer,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // ExoPlayer view
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.5f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Title
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Daily Quote Card (same as original)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RelaxQuoteCard(quote: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = AccentIndigo.copy(0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(AccentIndigo, AccentPurple)))
    ) {
        Box(
            modifier = Modifier.size(100.dp).align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp).background(Color.White.copy(0.07f), CircleShape)
        )
        Column(modifier = Modifier.padding(24.dp)) {
            Text("💬", fontSize = 32.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "\"$quote\"",
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                lineHeight = 24.sp, textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White.copy(0.20f)),
                    contentAlignment = Alignment.Center
                ) { Text("🌸", fontSize = 12.sp) }
                Text("Daily Wellness Reminder", fontSize = 12.sp, color = Color.White.copy(0.75f), fontWeight = FontWeight.Medium)
            }
        }
    }
}