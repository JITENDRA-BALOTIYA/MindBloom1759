package com.example.mental_health.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mental_health.ui.theme.DeepPurple
import com.example.mental_health.ui.viewmodel.AttendanceState
import com.example.mental_health.ui.viewmodel.AttendanceViewModel
import com.example.mental_health.ui.viewmodel.ErrorType
import com.example.mental_health.util.FaceAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceAttendanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val context         = LocalContext.current
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val locationInfo    by viewModel.locationInfo.collectAsStateWithLifecycle()

    // ── CAMERA PERMISSION ─────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // ── LOCATION PERMISSION ───────────────────────────────────
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Request both permissions on screen launch
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!hasLocationPermission) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ── LOCAL UI STATE ────────────────────────────────────────
    var faceDetected  by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Position your face in the frame") }

    // Update status message based on permission state
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            statusMessage = "Location permission required for attendance"
        } else {
            statusMessage = "Position your face in the frame"
        }
    }

    LaunchedEffect(attendanceState) {
        when (val s = attendanceState) {
            is AttendanceState.Success -> {
                Toast.makeText(context, "Attendance Marked Successfully!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is AttendanceState.Error -> {
                faceDetected  = false
                statusMessage = "Position your face in the frame"
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color.Transparent,
                    titleContentColor = DeepPurple
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(colors = listOf(Color(0xFFF3E8FF), Color.White))
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                text       = "Verification Step",
                style      = MaterialTheme.typography.headlineSmall,
                color      = DeepPurple,
                fontWeight = FontWeight.Bold
            )

            Text(
                text      = statusMessage,
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color.Gray,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )

            // Location permission warning banner
            if (!hasLocationPermission) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint     = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text     = "Location permission needed. Please grant it to mark attendance.",
                            fontSize = 12.sp,
                            color    = Color(0xFFE65100)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Button to re-request permission
                OutlinedButton(
                    onClick = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Grant Location Permission")
                }
            }

            // Location distance pill (shown once GPS is read)
            if (locationInfo.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFEDE7F6), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = DeepPurple, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(locationInfo, fontSize = 12.sp, color = DeepPurple)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── CAMERA CIRCLE ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = when {
                            !hasLocationPermission            -> Color(0xFFFF9800)
                            attendanceState is AttendanceState.Success -> Color(0xFF4CAF50)
                            attendanceState is AttendanceState.Error   -> Color(0xFFE53935)
                            else                                       -> DeepPurple
                        },
                        shape = CircleShape
                    )
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission && attendanceState !is AttendanceState.Success) {
                    CameraPreviewForAttendance(
                        onFaceDetected = {
                            // Guard: if location permission missing, prompt user
                            if (!hasLocationPermission) {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                return@CameraPreviewForAttendance
                            }
                            if (!faceDetected && attendanceState !is AttendanceState.Loading) {
                                faceDetected  = true
                                statusMessage = "Face detected! Verifying time & location…"
                                viewModel.markAttendance()
                            }
                        }
                    )
                } else if (!hasCameraPermission) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Face, contentDescription = null,
                            modifier = Modifier.size(80.dp), tint = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "Camera permission\nrequired",
                            fontSize  = 12.sp,
                            color     = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Face, contentDescription = null,
                        modifier = Modifier.size(100.dp), tint = Color.Gray
                    )
                }

                ScanningOverlay(isScanning = faceDetected && attendanceState is AttendanceState.Loading)
            }

            Spacer(Modifier.height(40.dp))

            // ── STATE-DRIVEN FEEDBACK ─────────────────────────
            when (val s = attendanceState) {
                is AttendanceState.Loading -> {
                    CircularProgressIndicator(color = DeepPurple)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text     = "Checking time window & location…",
                        color    = DeepPurple,
                        fontSize = 14.sp
                    )
                }

                is AttendanceState.Success -> {
                    Icon(
                        Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Verified!", color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold, fontSize = 20.sp
                    )
                }

                is AttendanceState.Error -> {
                    ErrorCard(
                        message   = s.message,
                        errorType = s.errorType,
                        onRetry   = {
                            viewModel.resetState()
                            // If it was a permission error, re-request
                            if (s.errorType == ErrorType.PERMISSION) {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }

                else -> Unit
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ERROR CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(
    message: String,
    errorType: ErrorType,
    onRetry: () -> Unit
) {
    val (bgColor, textColor, title) = when (errorType) {
        ErrorType.TIME_WINDOW -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Outside Attendance Window")
        ErrorType.LOCATION,
        ErrorType.PERMISSION  -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Location Verification Failed")
        ErrorType.GENERAL     -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Attendance Error")
    }

    Card(
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = textColor,
                modifier           = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text      = message,
                fontSize  = 13.sp,
                color     = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) { Text("Try Again") }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// CAMERA PREVIEW
// ─────────────────────────────────────────────────────────────

@Composable
fun CameraPreviewForAttendance(onFaceDetected: () -> Unit) {
    val context              = LocalContext.current
    val lifecycleOwner       = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor             = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, FaceAnalyzer { _ -> onFaceDetected() })
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) { e.printStackTrace() }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ─────────────────────────────────────────────────────────────
// SCANNING OVERLAY
// ─────────────────────────────────────────────────────────────

@Composable
fun ScanningOverlay(isScanning: Boolean) {
    AnimatedVisibility(
        visible = isScanning,
        enter   = fadeIn(),
        exit    = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 4.dp, color = DeepPurple.copy(alpha = 0.5f), shape = CircleShape)
        )
    }
}