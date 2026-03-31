package com.example.mental_health.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
// COLOR PALETTE  ← customize here
// ─────────────────────────────────────────────────────────────
private val GradientStart  = Color(0xFF4C1D95)
private val GradientMid    = Color(0xFF7C3AED)
private val GradientEnd    = Color(0xFFC084FC)
private val IconBg         = Color(0x26FFFFFF)   // white 15%
private val RingColor      = Color(0x59FFFFFF)   // white 35%
private val ProgressBg     = Color(0x26FFFFFF)   // white 15%
private val ProgressFg     = Color(0xBFFFFFFF)   // white 75%

@Composable
fun SplashScreen(onNext: () -> Unit) {

    // ── LOGO: overshoot scale ──────────────────────────────────
    val logoScale = remember { Animatable(0f) }

    // ── TAGLINE + dots: fade + slide up ───────────────────────
    val textAlpha     = remember { Animatable(0f) }
    val textOffsetY   = remember { Animatable(20f) }

    // ── Progress bar fill (0→1 over ~1800ms) ──────────────────
    val progress = remember { Animatable(0f) }

    // ── Pulsing rings ─────────────────────────────────────────
    val ring1Scale = rememberInfiniteTransition(label = "r1")
        .animateFloat(
            initialValue = 0.9f, targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "r1s"
        )
    val ring1Alpha = rememberInfiniteTransition(label = "ra1")
        .animateFloat(
            initialValue = 0.6f, targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "r1a"
        )
    val ring2Scale = rememberInfiniteTransition(label = "r2")
        .animateFloat(
            initialValue = 0.9f, targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, delayMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "r2s"
        )
    val ring2Alpha = rememberInfiniteTransition(label = "ra2")
        .animateFloat(
            initialValue = 0.6f, targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, delayMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "r2a"
        )

    // ── ORCHESTRATE ───────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Logo bounces in
        logoScale.animateTo(
            targetValue  = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
        // Text fades up
        delay(100)
        textAlpha.animateTo(1f,   animationSpec = tween(600))
        textOffsetY.animateTo(0f, animationSpec = tween(600))

        // Progress bar sweeps across
        delay(200)
        progress.animateTo(1f, animationSpec = tween(1800, easing = FastOutSlowInEasing))

        delay(300)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to GradientStart,
                        0.50f to GradientMid,
                        1.00f to GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Decorative blobs ───────────────────────────────────
        Box(
            Modifier
                .size(300.dp)
                .offset(x = 80.dp, y = (-120).dp)
                .alpha(0.06f)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopEnd)
        )
        Box(
            Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .alpha(0.05f)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.BottomStart)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxSize()
        ) {

            Spacer(Modifier.weight(1f))

            // ── Logo + pulsing rings ───────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier.size(120.dp)
            ) {
                // Ring 1
                PulsingRing(
                    size  = 120.dp,
                    scale = ring1Scale.value,
                    alpha = ring1Alpha.value,
                    color = RingColor
                )
                // Ring 2 (offset phase)
                PulsingRing(
                    size  = 120.dp,
                    scale = ring2Scale.value,
                    alpha = ring2Alpha.value,
                    color = RingColor
                )

                // Icon circle
                Box(
                    modifier         = Modifier
                        .size(100.dp)
                        .scale(logoScale.value)
                        .clip(CircleShape)
                        .background(IconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.SelfImprovement,
                        contentDescription = "MindBloom Logo",
                        modifier           = Modifier.size(56.dp),
                        tint               = Color.White
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── App name ───────────────────────────────────────
            Text(
                text       = "MindBloom",
                color      = Color.White,
                fontSize   = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier   = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )

            Spacer(Modifier.height(6.dp))

            // ── Tagline with letter-spaced uppercase style ─────
            Text(
                text      = "AI  MENTAL  WELLNESS",
                color     = Color.White.copy(alpha = 0.7f),
                fontSize  = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )

            Spacer(Modifier.weight(1f))

            // ── Dot indicators ─────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 20.dp)
            ) {
                listOf(1f, 0.4f, 0.4f).forEach { a ->
                    Box(
                        Modifier
                            .size(8.dp)
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = a))
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }

            // ── Progress bar ───────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 64.dp)
                    .padding(bottom = 56.dp)
                    .alpha(textAlpha.value)
            ) {
                // Background track
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(ProgressBg)
                )
                // Filled portion
                Box(
                    Modifier
                        .fillMaxWidth(progress.value)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(ProgressFg)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PULSING RING COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
private fun PulsingRing(
    size  : Dp,
    scale : Float,
    alpha : Float,
    color : Color
) {
    Box(
        Modifier
            .size(size)
            .scale(scale)
            .alpha(alpha)
            .clip(CircleShape)
            .background(Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// OvershootInterpolator (kept for backward compat, not used)
// ─────────────────────────────────────────────────────────────

class OvershootInterpolator(private val tension: Float) : android.view.animation.Interpolator {
    override fun getInterpolation(t: Float): Float {
        var v = t - 1.0f
        return v * v * ((tension + 1) * v + tension) + 1.0f
    }
}
