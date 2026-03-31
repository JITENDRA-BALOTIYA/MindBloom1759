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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.ui.viewmodel.AuthState
import com.example.mental_health.ui.viewmodel.AuthViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Apple Color System
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val BgField      = Color(0xFFF2F2F7)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentIndigoL= Color(0xFF7C7CFF)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentGreen  = Color(0xFF34C759)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Label3       = Color(0xFFC7C7CC)
private val Separator    = Color(0xFFE5E5EA)

// ─────────────────────────────────────────────────────────────────────────────
//  Step meta
// ─────────────────────────────────────────────────────────────────────────────
private val stepLabels = listOf("Personal", "Academic", "Emergency", "Account")
private val stepSubtitles = listOf(
    "Tell us about yourself",
    "Your academic background",
    "Guardian & emergency info",
    "Set up your credentials"
)

// ─────────────────────────────────────────────────────────────────────────────
//  Register Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val authState   by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Step 1 — Personal
    var name             by remember { mutableStateOf("") }
    var age              by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("") }
    var genderExpanded   by remember { mutableStateOf(false) }

    // Step 2 — Academic
    var collegeName      by remember { mutableStateOf("") }
    var course           by remember { mutableStateOf("") }
    var year             by remember { mutableStateOf("") }
    var rollNumber       by remember { mutableStateOf("") }  // ← added

    // Step 3 — Emergency
    var parentName       by remember { mutableStateOf("") }  // ← was missing
    var parentPhone      by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    // Step 4 — Account
    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var showPassword     by remember { mutableStateOf(false) }
    var showConfirmPwd   by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Registration Successful! 🎉", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Ambient blobs
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-70).dp, y = (-60).dp)
                .blur(120.dp)
                .background(
                    Brush.radialGradient(listOf(AccentIndigo.copy(alpha = 0.10f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 280.dp, y = 80.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(listOf(AccentPurple.copy(alpha = 0.09f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AppleRegisterHeader(
                currentStep = currentStep,
                onBack = { if (currentStep > 1) currentStep-- else onNavigateToLogin() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut(tween(150)))
                        } else {
                            (slideInHorizontally { -it } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally { it } + fadeOut(tween(150)))
                        }
                    },
                    label = "stepContent"
                ) { step ->
                    when (step) {
                        1 -> ApplePersonalStep(
                            name = name, onNameChange = { name = it },
                            age = age, onAgeChange = { age = it },
                            gender = gender, onGenderChange = { gender = it },
                            expanded = genderExpanded, onExpandedChange = { genderExpanded = it }
                        )
                        2 -> AppleAcademicStep(
                            collegeName = collegeName, onCollegeChange = { collegeName = it },
                            course = course, onCourseChange = { course = it },
                            year = year, onYearChange = { year = it },
                            rollNumber = rollNumber, onRollChange = { rollNumber = it }  // ← added
                        )
                        3 -> AppleEmergencyStep(
                            parentName = parentName, onPNameChange = { parentName = it },
                            parentPhone = parentPhone, onPPhoneChange = { parentPhone = it },
                            emergencyContact = emergencyContact, onEContactChange = { emergencyContact = it }
                        )
                        4 -> AppleAccountStep(
                            email = email, onEmailChange = { email = it },
                            password = password, onPasswordChange = { password = it },
                            confirmPassword = confirmPassword, onConfirmChange = { confirmPassword = it },
                            showPassword = showPassword, onTogglePassword = { showPassword = !showPassword },
                            showConfirm = showConfirmPwd, onToggleConfirm = { showConfirmPwd = !showConfirmPwd }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }

            // ── Bottom navigation ─────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentStep > 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BgCard)
                                .border(1.dp, Separator, RoundedCornerShape(16.dp))
                                .clickable { currentStep-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("← Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Label1)
                        }
                    }

                    val isLastStep = currentStep == 4
                    val isLoading  = authState is AuthState.Loading

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = AccentIndigo.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (!isLoading)
                                    Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL))
                                else
                                    Brush.linearGradient(listOf(Label3, Label3))
                            )
                            .clickable(enabled = !isLoading) {
                                if (!isLastStep) {
                                    // Step-wise validation before proceeding
                                    val stepError = when (currentStep) {
                                        1 -> when {
                                            name.isBlank()   -> "Please enter your name"
                                            age.isBlank()    -> "Please enter your age"
                                            gender.isBlank() -> "Please select your gender"
                                            else             -> null
                                        }
                                        2 -> when {
                                            collegeName.isBlank() -> "Please enter college name"
                                            course.isBlank()      -> "Please enter your course"
                                            year.isBlank()        -> "Please enter your year"
                                            else                  -> null
                                        }
                                        3 -> when {
                                            parentName.isBlank()  -> "Please enter parent name"
                                            parentPhone.isBlank() -> "Please enter parent phone"
                                            else                  -> null
                                        }
                                        else -> null
                                    }
                                    if (stepError != null) {
                                        Toast.makeText(context, stepError, Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentStep++
                                    }
                                } else {
                                    // Final submit — validate step 4
                                    when {
                                        email.isBlank() ->
                                            Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show()
                                        password.isBlank() ->
                                            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
                                        password.length < 6 ->
                                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                        password != confirmPassword ->
                                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                        else -> viewModel.registerStudent(
                                            name             = name.trim(),
                                            age              = age.toIntOrNull() ?: 0,
                                            gender           = gender,
                                            collegeName      = collegeName.trim(),
                                            course           = course.trim(),
                                            year             = year.trim(),
                                            rollNumber       = rollNumber.trim(),
                                            parentName       = parentName.trim(),
                                            parentPhone      = parentPhone.trim(),
                                            emergencyContact = emergencyContact.trim(),
                                            email            = email.trim(),
                                            password         = password
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                if (isLastStep) "Create Account 🎉" else "Next Step →",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier                = Modifier.fillMaxWidth(),
                    horizontalArrangement   = Arrangement.Center,
                    verticalAlignment       = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", fontSize = 14.sp, color = Label2)
                    TextButton(
                        onClick        = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Login", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentIndigo)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Header + Step Indicator (unchanged)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleRegisterHeader(currentStep: Int, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(AccentIndigo.copy(alpha = 0.10f))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = AccentIndigo, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "Create Account",
            fontSize      = 26.sp,
            fontWeight    = FontWeight.ExtraBold,
            color         = Label1,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Step $currentStep of 4 · ${stepSubtitles[currentStep - 1]}",
            fontSize   = 13.sp,
            color      = Label2,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val step        = index + 1
                val isCompleted = step < currentStep
                val isActive    = step == currentStep

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            if (isActive) 8.dp else 0.dp,
                            CircleShape,
                            spotColor = AccentIndigo.copy(alpha = 0.35f)
                        )
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive    -> Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL))
                                isCompleted -> Brush.linearGradient(listOf(AccentGreen, Color(0xFF30D158)))
                                else        -> Brush.linearGradient(listOf(Separator, Separator))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isCompleted,
                        transitionSpec = {
                            scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
                                    scaleOut() + fadeOut()
                        },
                        label = "stepIcon$step"
                    ) { completed ->
                        if (completed) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                "$step",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isActive) Color.White else Label2
                            )
                        }
                    }
                }

                if (index < 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (step < currentStep)
                                    Brush.horizontalGradient(listOf(AccentGreen, AccentIndigo))
                                else
                                    Brush.horizontalGradient(listOf(Separator, Separator))
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            stepLabels.forEachIndexed { index, label ->
                Text(
                    label,
                    fontSize   = 9.sp,
                    fontWeight = if (index + 1 == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color      = if (index + 1 == currentStep) AccentIndigo else Label2,
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 1 — Personal
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplePersonalStep(
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit
) {
    AppleFormCard(title = "Personal Details", subtitle = "Tell us a bit about yourself") {
        AppleField(value = name, onValueChange = onNameChange, label = "FULL NAME", emoji = "👤", placeholder = "Enter your full name")
        AppleField(value = age,  onValueChange = onAgeChange,  label = "AGE",       emoji = "🎂", placeholder = "Your age", keyboardType = KeyboardType.Number)

        Column {
            Text("GENDER", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Label2, letterSpacing = 0.4.sp)
            Spacer(Modifier.height(5.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgField)
                        .menuAnchor()
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧑", fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            gender.ifEmpty { "Select Gender" },
                            fontSize = 15.sp,
                            color    = if (gender.isEmpty()) Label3 else Label1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null, tint = Label2, modifier = Modifier.size(18.dp)
                        )
                    }
                }
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                    listOf("Male", "Female", "Non-Binary", "Prefer not to say").forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option, fontSize = 15.sp, color = Label1) },
                            onClick = { onGenderChange(option); onExpandedChange(false) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 2 — Academic  (roll number added)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleAcademicStep(
    collegeName: String, onCollegeChange: (String) -> Unit,
    course: String, onCourseChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    rollNumber: String, onRollChange: (String) -> Unit   // ← added
) {
    AppleFormCard(title = "Academic Background", subtitle = "Your college and course info") {
        AppleField(value = collegeName, onValueChange = onCollegeChange, label = "COLLEGE NAME",        emoji = "🏫", placeholder = "Your college")
        AppleField(value = course,      onValueChange = onCourseChange,  label = "COURSE / DEPARTMENT", emoji = "📚", placeholder = "e.g. B.Tech Computer Science")
        AppleField(value = year,        onValueChange = onYearChange,    label = "YEAR OF STUDY",       emoji = "📅", placeholder = "e.g. 3", keyboardType = KeyboardType.Number)
        AppleField(value = rollNumber,  onValueChange = onRollChange,    label = "ROLL NUMBER",         emoji = "🔖", placeholder = "e.g. 21CS001")   // ← added
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 3 — Emergency
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleEmergencyStep(
    parentName: String, onPNameChange: (String) -> Unit,
    parentPhone: String, onPPhoneChange: (String) -> Unit,
    emergencyContact: String, onEContactChange: (String) -> Unit
) {
    AppleFormCard(title = "Emergency Contacts", subtitle = "Guardian & emergency contact info") {
        AppleField(value = parentName,       onValueChange = onPNameChange,   label = "PARENT / GUARDIAN NAME",  emoji = "👨‍👩‍👧", placeholder = "Parent's full name")
        AppleField(value = parentPhone,      onValueChange = onPPhoneChange,  label = "PARENT PHONE",            emoji = "📞",    placeholder = "+91 XXXXX XXXXX",    keyboardType = KeyboardType.Phone)
        AppleField(value = emergencyContact, onValueChange = onEContactChange,label = "OTHER EMERGENCY CONTACT", emoji = "🆘",    placeholder = "Alternative number", keyboardType = KeyboardType.Phone)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 4 — Account
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleAccountStep(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmChange: (String) -> Unit,
    showPassword: Boolean, onTogglePassword: () -> Unit,
    showConfirm: Boolean, onToggleConfirm: () -> Unit
) {
    AppleFormCard(title = "Account Security", subtitle = "Set your login credentials") {
        AppleField(value = email, onValueChange = onEmailChange, label = "EMAIL ADDRESS", emoji = "✉️", placeholder = "your@email.com", keyboardType = KeyboardType.Email)
        ApplePasswordField(value = password,        onValueChange = onPasswordChange, label = "PASSWORD",         visible = showPassword, onToggle = onTogglePassword)
        ApplePasswordField(value = confirmPassword, onValueChange = onConfirmChange,  label = "CONFIRM PASSWORD", visible = showConfirm,  onToggle = onToggleConfirm)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared: Form Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleFormCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.07f))
            .clip(RoundedCornerShape(28.dp))
            .background(BgCard)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(title,    fontSize = 18.sp, fontWeight = FontWeight.Bold,   color = Label1)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = Label2)
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared: Input Field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppleField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    emoji: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Label2, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgField)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value          = value,
                onValueChange  = onValueChange,
                modifier       = Modifier.weight(1f),
                singleLine     = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle      = TextStyle(fontSize = 15.sp, color = Label1, fontWeight = FontWeight.Medium),
                decorationBox  = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 15.sp, color = Label3)
                    inner()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared: Password Field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ApplePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggle: () -> Unit
) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Label2, letterSpacing = 0.4.sp)
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgField)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔒", fontSize = 16.sp)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value               = value,
                onValueChange       = onValueChange,
                modifier            = Modifier.weight(1f),
                singleLine          = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions     = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle           = TextStyle(fontSize = 15.sp, color = Label1, fontWeight = FontWeight.Medium),
                decorationBox       = { inner ->
                    if (value.isEmpty()) Text("Enter password", fontSize = 15.sp, color = Label3)
                    inner()
                }
            )
            TextButton(onClick = onToggle, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Text(
                    if (visible) "Hide" else "Show",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AccentIndigo
                )
            }
        }
    }
}