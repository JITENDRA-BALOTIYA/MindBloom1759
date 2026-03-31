package com.example.mental_health.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.ui.viewmodel.AuthState
import com.example.mental_health.ui.viewmodel.AuthViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Apple Color System
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary     = Color(0xFFF2F2F7)
private val BgCard        = Color(0xFFFFFFFF)
private val BgField       = Color(0xFFF2F2F7)
private val AccentIndigo  = Color(0xFF5E5CE6)
private val AccentIndigoL = Color(0xFF7C7CFF)
private val AccentPurple  = Color(0xFFAF52DE)
private val Label1        = Color(0xFF1C1C1E)
private val Label2        = Color(0xFF8E8E93)
private val Label3        = Color(0xFFC7C7CC)
private val Separator     = Color(0xFFE5E5EA)

// ─────────────────────────────────────────────────────────────────────────────
//  Responsive helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Screen size buckets used for responsive layout decisions.
 */
enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberScreenSize(): ScreenSize {
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp) {
        when {
            config.screenWidthDp < 600  -> ScreenSize.COMPACT   // phones
            config.screenWidthDp < 840  -> ScreenSize.MEDIUM    // small tablets / foldables
            else                        -> ScreenSize.EXPANDED  // large tablets / desktops
        }
    }
}

/** Returns a value that scales between [min] and [max] based on screen size. */
@Composable
fun responsiveDp(compact: Dp, medium: Dp, expanded: Dp): Dp {
    return when (rememberScreenSize()) {
        ScreenSize.COMPACT  -> compact
        ScreenSize.MEDIUM   -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Login Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState       by viewModel.authState.collectAsState()
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context         = LocalContext.current
    val screenSize      = rememberScreenSize()
    val scrollState     = rememberScrollState()

    // Responsive sizing values
    val horizontalPadding = responsiveDp(compact = 16.dp, medium = 40.dp, expanded = 0.dp)
    val cardMaxWidth      = when (screenSize) {
        ScreenSize.COMPACT  -> 480.dp
        ScreenSize.MEDIUM   -> 520.dp
        ScreenSize.EXPANDED -> 480.dp
    }
    val cardPaddingH      = responsiveDp(compact = 20.dp, medium = 28.dp, expanded = 36.dp)
    val cardPaddingV      = responsiveDp(compact = 24.dp, medium = 32.dp, expanded = 40.dp)
    val topSpacing        = responsiveDp(compact = 32.dp, medium = 48.dp, expanded = 60.dp)
    val iconSize          = responsiveDp(compact = 70.dp, medium = 80.dp, expanded = 88.dp)
    val iconCorner        = responsiveDp(compact = 20.dp, medium = 22.dp, expanded = 24.dp)
    val appNameSize       = when (screenSize) {
        ScreenSize.COMPACT  -> 26.sp
        ScreenSize.MEDIUM   -> 30.sp
        ScreenSize.EXPANDED -> 34.sp
    }
    val buttonHeight      = responsiveDp(compact = 50.dp, medium = 54.dp, expanded = 58.dp)
    val fieldCorner       = responsiveDp(compact = 12.dp, medium = 14.dp, expanded = 16.dp)

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Welcome back! 🌿", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState as AuthState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // ── Ambient blobs ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(if (screenSize == ScreenSize.COMPACT) 260.dp else 380.dp)
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = (-80).dp)
                .blur(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentIndigo.copy(alpha = 0.13f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(if (screenSize == ScreenSize.COMPACT) 200.dp else 300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-160).dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentPurple.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(topSpacing))

            // ── App Icon + Branding ───────────────────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0.7f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .shadow(
                            16.dp,
                            RoundedCornerShape(iconCorner),
                            spotColor = AccentIndigo.copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(iconCorner))
                        .background(
                            Brush.linearGradient(listOf(AccentIndigo, AccentPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize * 0.52f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "MindBloom",
                fontSize = appNameSize,
                fontWeight = FontWeight.ExtraBold,
                color = Label1,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Mental Wellness Starts Here",
                fontSize = if (screenSize == ScreenSize.COMPACT) 13.sp else 15.sp,
                color = Label2,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(responsiveDp(compact = 28.dp, medium = 36.dp, expanded = 44.dp)))

            // ── Form Card ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = cardMaxWidth)           // caps width on large screens
                    .padding(horizontal = horizontalPadding)
                    .shadow(
                        8.dp,
                        RoundedCornerShape(28.dp),
                        spotColor = Color.Black.copy(alpha = 0.07f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(BgCard)
                    .padding(horizontal = cardPaddingH, vertical = cardPaddingV)
            ) {
                Column {
                    Text(
                        "Welcome Back 👋",
                        fontSize = if (screenSize == ScreenSize.COMPACT) 20.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Label1
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to continue your journey",
                        fontSize = 13.sp,
                        color = Label2
                    )

                    Spacer(Modifier.height(22.dp))

                    // Email
                    AppleInputField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL ADDRESS",
                        icon = Icons.Default.Email,
                        fieldCorner = fieldCorner,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    AppleInputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        icon = Icons.Default.Lock,
                        fieldCorner = fieldCorner,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Forgot password
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = {
                            Toast.makeText(
                                context,
                                "Forgot Password feature coming soon ✨",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Text(
                                "Forgot Password?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentIndigo
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Login button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .shadow(
                                10.dp,
                                RoundedCornerShape(16.dp),
                                spotColor = AccentIndigo.copy(alpha = 0.40f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL))
                            )
                            .clickable { viewModel.login(email, password) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Login Securely →",
                                fontSize = if (screenSize == ScreenSize.COMPACT) 15.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Separator)
                        )
                        Text(
                            "  or continue with  ",
                            fontSize = 12.sp,
                            color = Label2,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(Separator)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Google button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(responsiveDp(compact = 48.dp, medium = 52.dp, expanded = 54.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgField)
                            .border(1.dp, Separator, RoundedCornerShape(14.dp))
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "Google Sign-In coming soon 🔗",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = AccentIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Continue with Google",
                                fontSize = if (screenSize == ScreenSize.COMPACT) 14.sp else 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Label1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Register link
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New here?", fontSize = 14.sp, color = Label2)
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        "Create Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Apple Input Field  (now accepts fieldCorner for responsive radius)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    fieldCorner: Dp = 14.dp,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Label2,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(fieldCorner))
                .background(BgField)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Label2,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp),
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = if (isPassword && !passwordVisible)
                        PasswordVisualTransformation()
                    else
                        VisualTransformation.None,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = Label1,
                        fontWeight = FontWeight.Medium
                    ),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                if (isPassword) "Enter your password" else "Enter your email",
                                fontSize = 15.sp,
                                color = Label3
                            )
                        }
                        inner()
                    }
                )
                if (isPassword) {
                    TextButton(
                        onClick = onPasswordToggle,
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Text(
                            if (passwordVisible) "Hide" else "Show",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentIndigo
                        )
                    }
                }
            }
        }
    }
}