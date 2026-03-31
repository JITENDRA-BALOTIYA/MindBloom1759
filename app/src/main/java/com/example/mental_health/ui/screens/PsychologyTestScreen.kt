@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mental_health.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  Color System
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentRed    = Color(0xFFFF3B30)
private val AccentRedL   = Color(0xFFFF6B6B)
private val AccentAmber  = Color(0xFFFF9F0A)
private val AccentGreen  = Color(0xFF34C759)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Separator    = Color(0xFFE5E5EA)

// ─────────────────────────────────────────────────────────────────────────────
//  Data Model
// ─────────────────────────────────────────────────────────────────────────────
data class Question(val text: String, val options: List<String>)

private val questions = listOf(
    Question("How often have you been upset because of something that happened unexpectedly?",
        listOf("Never", "Almost Never", "Sometimes", "Fairly Often", "Very Often")),
    Question("How often have you felt unable to control the important things in your life?",
        listOf("Never", "Almost Never", "Sometimes", "Fairly Often", "Very Often")),
    Question("How often have you felt nervous and 'stressed'?",
        listOf("Never", "Almost Never", "Sometimes", "Fairly Often", "Very Often")),
    Question("How often have you felt confident about your ability to handle your personal problems?",
        listOf("Never", "Almost Never", "Sometimes", "Fairly Often", "Very Often")),
    Question("How often have you felt that things were going your way?",
        listOf("Never", "Almost Never", "Sometimes", "Fairly Often", "Very Often")),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PsychologyTestScreen(onBack: () -> Unit) {
    var score                by remember { mutableIntStateOf(0) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption       by remember { mutableStateOf<Int?>(null) }
    var showResult           by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ────────────────────────────────────────────────────────
            TestTopBar(onBack = onBack)

            // ── Content ────────────────────────────────────────────────────────
            AnimatedContent(
                targetState = showResult,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally { it }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally { -it })
                },
                label = "screenTransition"
            ) { isResult ->
                if (!isResult) {
                    QuestionScreen(
                        question = questions[currentQuestionIndex],
                        questionIndex = currentQuestionIndex,
                        totalQuestions = questions.size,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        onNext = {
                            selectedOption?.let { sel ->
                                score += sel
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOption = null
                                } else {
                                    showResult = true
                                }
                            }
                        }
                    )
                } else {
                    ResultScreen(
                        score = score,
                        onBack = onBack,
                        onRetake = {
                            score = 0
                            currentQuestionIndex = 0
                            selectedOption = null
                            showResult = false
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TestTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .border(BorderStroke(0.5.dp, Separator))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(alpha = 0.10f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Stress Assessment", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Label1)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(34.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).align(Alignment.BottomCenter).background(Separator))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Question Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuestionScreen(
    question: Question,
    questionIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Progress bar + counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Animated progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Separator)
            ) {
                val progress by animateFloatAsState(
                    targetValue = (questionIndex + 1f) / totalQuestions,
                    animationSpec = tween(400),
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(AccentRed, AccentRedL)))
                )
            }
            Text(
                "${questionIndex + 1}/$totalQuestions",
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentRed
            )
        }

        Spacer(Modifier.height(10.dp))

        // Step pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalQuestions) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i <= questionIndex) AccentRed else Separator)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Question Card
        AnimatedContent(
            targetState = questionIndex,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally { -it } + fadeOut(tween(150)))
            },
            label = "questionSlide"
        ) { idx ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = AccentRed.copy(0.12f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgCard)
                    .padding(22.dp)
            ) {
                Column {
                    // Q number badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AccentRed, AccentRedL))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            "Question ${idx + 1} of $totalQuestions",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Label2
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        questions[idx].text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Label1,
                        lineHeight = 26.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Options
        question.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == index
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                label = "optScale$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .shadow(
                        if (isSelected) 6.dp else 1.dp,
                        RoundedCornerShape(14.dp),
                        spotColor = if (isSelected) AccentRed.copy(0.25f) else Color.Transparent
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) AccentRed.copy(alpha = 0.06f) else BgCard
                    )
                    .border(
                        BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) AccentRed.copy(0.35f) else Separator
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Radio circle
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected)
                                    Modifier.background(Brush.linearGradient(listOf(AccentRed, AccentRedL)))
                                else
                                    Modifier.border(2.dp, Separator, CircleShape).background(BgCard)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    }

                    Text(
                        option,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AccentRed else Label1
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Next button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    if (selectedOption != null) 8.dp else 0.dp,
                    RoundedCornerShape(16.dp),
                    spotColor = AccentRed.copy(0.35f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selectedOption != null)
                        Brush.linearGradient(listOf(AccentRed, AccentRedL))
                    else
                        Brush.linearGradient(listOf(Separator, Separator))
                )
                .clickable(enabled = selectedOption != null) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (questionIndex < questions.size - 1) "Next Question →" else "See Results →",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedOption != null) Color.White else Label2
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Result Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ResultScreen(score: Int, onBack: () -> Unit, onRetake: () -> Unit) {
    val maxScore = (questions.size - 1) * 4
    val percentage = (score.toFloat() / maxScore * 100).toInt().coerceIn(0, 100)

    val (riskLevel, riskEmoji, riskColor, riskGradient, description) = when {
        score < 8  -> ResultData("Low Stress",      "😌", AccentGreen,  listOf(AccentGreen, Color(0xFF30D158)),
            "Great job! Your stress levels are well-managed. Keep up your healthy habits and self-care routines.")
        score < 15 -> ResultData("Moderate Stress", "😐", AccentAmber,  listOf(AccentAmber, Color(0xFFFF6B00)),
            "You're experiencing some stress. Consider using our Relax Mode and breathing exercises regularly.")
        else       -> ResultData("High Stress",     "😰", AccentRed,    listOf(AccentRed, AccentRedL),
            "Your stress levels are high. We strongly recommend talking to a counselor and using our Relax Mode features.")
    }

    // Animated score counter
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )
    val animatedPct by animateIntAsState(
        targetValue = percentage,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "pctAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Result hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = riskColor.copy(0.3f))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(riskGradient))
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            // Decorative circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(Color.White.copy(0.07f), CircleShape)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(riskEmoji, fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("Your Results", fontSize = 14.sp, color = Color.White.copy(0.80f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(riskLevel, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                // Score circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$animatedScore", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("score", fontSize = 10.sp, color = Color.White.copy(0.80f))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Stress level bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stress Level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Label1)
                    Text("$animatedPct%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = riskColor)
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Separator)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPct / 100f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(riskGradient))
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("😌 Low", fontSize = 10.sp, color = Label2)
                    Text("😐 Moderate", fontSize = 10.sp, color = Label2)
                    Text("😰 High", fontSize = 10.sp, color = Label2)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Description card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(riskColor.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Text("💡", fontSize = 18.sp) }
                    Text("What This Means", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Label1)
                }
                Spacer(Modifier.height(12.dp))
                Text(description, fontSize = 14.sp, color = Label1, lineHeight = 22.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = AccentIndigo.copy(0.3f))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(AccentIndigo, Color(0xFF7C7CFF))))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("Go to Dashboard 🏠", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .border(BorderStroke(1.dp, Separator), RoundedCornerShape(16.dp))
                .clickable { onRetake() },
            contentAlignment = Alignment.Center
        ) {
            Text("Retake Test 🔄", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Label1)
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Result Data Helper
// ─────────────────────────────────────────────────────────────────────────────
private data class ResultData(
    val riskLevel: String,
    val emoji: String,
    val color: Color,
    val gradient: List<Color>,
    val description: String
)