@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.example.mental_health.ui.screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.data.model.MeditationSession
import com.example.mental_health.ui.viewmodel.*

// ─────────────────────────────────────────────────────────────────────────────
// Color Palette
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentGreen  = Color(0xFF34C759)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val GlassWhite   = Color.White.copy(alpha = 0.25f)

// ─────────────────────────────────────────────────────────────────────────────
// Main Meditation Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MeditationScreen(
    onBack: () -> Unit,
    viewModel: MeditationViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val activeState  by viewModel.activeSession.collectAsState()
    val heatmap      by viewModel.streakHeatmap.collectAsState()
    val goalProgress by viewModel.dailyGoalProgress.collectAsState()

    // ── FIX: TTS sirf MeditationScreen pe enable hoga ────────────────────
    LaunchedEffect(Unit) {
        viewModel.initTts()
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disableTts()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {
        ParallaxBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            PremiumHeroHeader(
                onBack       = onBack,
                heatmap      = heatmap,
                goalProgress = goalProgress
            )

            FilterSection(
                query        = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                categories   = uiState.categories,
                selected     = uiState.selectedCategory,
                onSelect     = viewModel::onCategorySelect
            )

            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (uiState.isLoading) {
                    item { LoadingState() }
                } else {
                    items(uiState.filteredSessions, key = { it.id }) { session ->
                        PremiumExerciseCard(
                            session          = session,
                            isFavorite       = uiState.favorites.contains(session.id),
                            onToggleFavorite = { viewModel.toggleFavorite(session.id) },
                            onStart          = { viewModel.startSession(session) }
                        )
                    }
                }
            }
        }

        // ── Overlays ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = activeState.status != SessionStatus.IDLE,
            enter   = slideInVertically { it } + fadeIn(),
            exit    = slideOutVertically { it } + fadeOut()
        ) {
            when (activeState.status) {
                SessionStatus.COUNTDOWN -> CountdownOverlay(activeState.countdown)

                SessionStatus.RUNNING, SessionStatus.PAUSED ->
                    ActiveSessionOverlay(
                        state        = activeState,
                        onPause      = viewModel::pauseSession,
                        onResume     = viewModel::resumeSession,
                        onClose      = viewModel::stopSession,
                        onSoundChange = viewModel::setAmbientSound,
                        formatTime   = viewModel::formatTime
                    )

                SessionStatus.COMPLETED ->
                    CompletionOverlay(
                        session   = activeState.session,
                        onDismiss = viewModel::stopSession
                    )

                else -> {}
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PremiumHeroHeader(
    onBack:       () -> Unit,
    heatmap:      Map<Int, Int>,
    goalProgress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(AccentIndigo, AccentPurple)))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.background(GlassWhite, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                    CircularProgressIndicator(
                        progress    = { goalProgress },
                        color       = Color.White,
                        strokeWidth = 3.dp,
                        trackColor  = Color.White.copy(0.2f)
                    )
                    Text("🔥", fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Inner Peace",                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Your meditation journey continues…", color = Color.White.copy(0.8f), fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))
            Text("Activity Streak", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                heatmap.toList().takeLast(14).forEach { (_, level) ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (level) {
                                    0    -> Color.White.copy(0.1f)
                                    1    -> AccentGreen.copy(0.4f)
                                    2    -> AccentGreen.copy(0.7f)
                                    else -> AccentGreen
                                }
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FilterSection(
    query:         String,
    onQueryChange: (String) -> Unit,
    categories:    List<String>,
    selected:      String,
    onSelect:      (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = Label2, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value       = query,
                    onValueChange = onQueryChange,
                    textStyle   = TextStyle(fontSize = 15.sp, color = Label1),
                    modifier    = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search exercises…", color = Label2, fontSize = 15.sp)
                        inner()
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = cat == selected
                Surface(
                    onClick       = { onSelect(cat) },
                    shape         = RoundedCornerShape(20.dp),
                    color         = if (isSelected) AccentIndigo else BgCard,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        cat,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color      = if (isSelected) Color.White else Label2,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Exercise Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PremiumExerciseCard(
    session:          MeditationSession,
    isFavorite:       Boolean,
    onToggleFavorite: () -> Unit,
    onStart:          () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onStart() },
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE0E0FF), Color(0xFFF0F0FF)))),
                contentAlignment = Alignment.Center
            ) { Text(session.emoji, fontSize = 32.sp) }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(session.title,    fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Label1)
                Text(session.subtitle, fontSize = 13.sp, color = Label2)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = AccentIndigo, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${session.durationMinutes} min", fontSize = 12.sp, color = AccentIndigo, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(Label2))
                    Spacer(Modifier.width(12.dp))
                    Text(session.level, fontSize = 12.sp, color = Label2)
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Label2
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overlays
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CountdownOverlay(count: Int) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState  = count,
            transitionSpec = {
                (scaleIn(tween(600)) + fadeIn()).togetherWith(scaleOut(tween(400)) + fadeOut())
            },
            label = "count"
        ) { targetCount ->
            Text(targetCount.toString(), color = Color.White, fontSize = 120.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ActiveSessionOverlay(
    state:        ActiveSessionState,
    onPause:      () -> Unit,
    onResume:     () -> Unit,
    onClose:      () -> Unit,
    onSoundChange: (AmbientSound) -> Unit,
    formatTime:   (Int) -> String
) {
    val isPaused = state.status == SessionStatus.PAUSED

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AccentIndigo.copy(0.9f), AccentPurple.copy(0.9f))))
            .statusBarsPadding()
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.session?.title ?: "", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(state.elapsedSeconds), color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
                IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
            }

            Spacer(Modifier.weight(1f))

            when (state.session?.category) {
                "Breathing" -> BreathingVisualizer(state.phase, state.phaseProgress)
                "Physical"  -> PhysicalGuideVisualizer(state.session.title, state.phase)
                else        -> DefaultVisualizer(state.phase)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                state.phase.name.replace("_", " "),
                color        = Color.White,
                fontSize     = 24.sp,
                fontWeight   = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.weight(1f))

            AmbientSoundRow(current = state.ambientSound, onSelect = onSoundChange)

            Spacer(Modifier.height(32.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick         = if (isPaused) onResume else onPause,
                    containerColor  = Color.White,
                    contentColor    = AccentIndigo,
                    shape           = CircleShape,
                    modifier        = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun BreathingVisualizer(phase: BreathingPhase, progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "glow"
    )
    val sizeScale by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 1.5f
            BreathingPhase.EXHALE -> 1.0f
            else                  -> 1.2f
        },
        animationSpec = tween(4000, easing = LinearOutSlowInEasing), label = "size"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp).scale(glowScale)) {
            drawCircle(
                brush  = Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)),
                radius = size.width / 2
            )
        }
        Canvas(modifier = Modifier.size(180.dp).scale(sizeScale)) {
            drawCircle(
                brush = Brush.linearGradient(listOf(Color.White, Color.White.copy(0.6f))),
                style = Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color      = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter  = false,
                style      = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text("🧘", fontSize = 48.sp)
    }
}

@Composable
fun PhysicalGuideVisualizer(title: String, phase: BreathingPhase) {
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        if (title.contains("Eye")) EyeVisualizer()
        else NeckVisualizer(phase)
    }
}

@Composable
fun EyeVisualizer() {
    val infinite = rememberInfiniteTransition(label = "eye")
    val eyeX by infinite.animateFloat(
        initialValue  = -30f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "x"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        listOf(-50f, 50f).forEach { offset ->
            drawCircle(Color.White, radius = 30.dp.toPx(), center = center.copy(x = center.x + offset))
            drawCircle(Color.Black, radius = 10.dp.toPx(), center = center.copy(x = center.x + offset + eyeX))
        }
    }
}

@Composable
fun NeckVisualizer(phase: BreathingPhase) {
    val rotation by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 15f
            BreathingPhase.EXHALE -> -15f
            else                  -> 0f
        },
        animationSpec = tween(3000), label = "neck"
    )
    Icon(
        Icons.Default.Face, null,
        modifier = Modifier.size(120.dp).graphicsLayer { rotationZ = rotation },
        tint     = Color.White
    )
}

@Composable
fun DefaultVisualizer(phase: BreathingPhase) {
    Text(
        "✨", fontSize = 100.sp,
        modifier = Modifier.scale(if (phase == BreathingPhase.INHALE) 1.2f else 1f)
    )
}

@Composable
fun AmbientSoundRow(current: AmbientSound, onSelect: (AmbientSound) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(AmbientSound.entries) { sound ->
            val isSelected = current == sound
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick  = { onSelect(sound) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (isSelected) Color.White else GlassWhite, CircleShape)
                ) {
                    Icon(
                        when (sound) {
                            AmbientSound.RAIN   -> Icons.Default.Cloud
                            AmbientSound.OCEAN  -> Icons.Default.Water
                            AmbientSound.FOREST -> Icons.Default.Park
                            AmbientSound.FIRE   -> Icons.Default.LocalFireDepartment
                            else                -> Icons.Default.MusicOff
                        },
                        null,
                        tint = if (isSelected) AccentIndigo else Color.White
                    )
                }
                Text(sound.label, color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun CompletionOverlay(session: MeditationSession?, onDismiss: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("✨", fontSize = 80.sp)
            Spacer(Modifier.height(24.dp))
            Text("Inner Peace Achieved", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(session?.title ?: "", color = Color.White.copy(0.6f), fontSize = 16.sp)
            Spacer(Modifier.height(48.dp))
            Button(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape    = RoundedCornerShape(16.dp)
            ) { Text("Return to Calm", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ParallaxBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "parallax")
    val offset1 by infiniteTransition.animateFloat(
        initialValue  = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing)), label = "o1"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(AccentIndigo.copy(alpha = 0.03f), 400f, Offset(offset1 % size.width, size.height * 0.2f))
        drawCircle(AccentPurple.copy(alpha = 0.03f), 600f, Offset(size.width - (offset1 % size.width), size.height * 0.7f))
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentIndigo)
    }
}