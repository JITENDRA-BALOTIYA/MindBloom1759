package com.example.mental_health.ui.screens



import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.data.model.RiskLevel
import com.example.mental_health.data.model.WeeklyReport
import com.example.mental_health.data.model.riskLevel
import com.example.mental_health.ui.viewmodel.AdminReportViewModel
import com.example.mental_health.ui.viewmodel.AdminSummaryStats
import com.example.mental_health.ui.viewmodel.ReportFilter

// ─────────────────────────────────────────────────────────────────────────────
//  Color palette (matches LoginScreen Apple system)
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val BgField      = Color(0xFFF2F2F7)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentPurple = Color(0xFFAF52DE)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Separator    = Color(0xFFE5E5EA)

private val RiskHighBg   = Color(0xFFFFE5E5)
private val RiskHighFg   = Color(0xFFD32F2F)
private val RiskMedBg    = Color(0xFFFFF3E0)
private val RiskMedFg    = Color(0xFFE65100)
private val RiskLowBg    = Color(0xFFE8F5E9)
private val RiskLowFg    = Color(0xFF2E7D32)

// ─────────────────────────────────────────────────────────────────────────────
//  Admin Report Screen (root)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportScreen(
    onBack: () -> Unit,
    viewModel: AdminReportViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val screenSize = rememberScreenSize()

    // Snackbar
    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPrimary)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentIndigo)
                    }
                    Column {
                        Text(
                            "Admin Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Label1
                        )
                        Text(
                            "Weekly student reports",
                            fontSize = 12.sp,
                            color = Label2
                        )
                    }
                }
            }

            Divider(color = Separator, thickness = 0.5.dp)

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentIndigo)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = if (screenSize == ScreenSize.EXPANDED) 40.dp else 16.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Summary Stats Banner
                    item {
                        SummaryStatsBanner(stats = viewModel.getSummaryStats())
                    }

                    // Search bar
                    item {
                        AdminSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange
                        )
                    }

                    // Filter chips
                    item {
                        FilterChipRow(
                            selected = uiState.selectedFilter,
                            onSelect = viewModel::onFilterChange
                        )
                    }

                    // Report count
                    item {
                        Text(
                            "${uiState.filteredReports.size} reports",
                            fontSize = 13.sp,
                            color = Label2,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Report cards
                    if (uiState.filteredReports.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        items(uiState.filteredReports, key = { it.reportId }) { report ->
                            ReportCard(
                                report = report,
                                onClick = { viewModel.selectReport(report) }
                            )
                        }
                    }

                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }

        // Snackbar
        AnimatedVisibility(
            visible = uiState.error != null || uiState.successMessage != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val isError = uiState.error != null
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                color = if (isError) RiskHighBg else RiskLowBg,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = uiState.error ?: uiState.successMessage ?: "",
                    color = if (isError) RiskHighFg else RiskLowFg,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Detail Dialog
    if (uiState.selectedReport != null) {
        ReportDetailDialog(
            report      = uiState.selectedReport!!,
            adminNote   = uiState.adminNoteInput,
            isSaving    = uiState.isSavingNote,
            onNoteChange= viewModel::onAdminNoteChange,
            onSaveNote  = viewModel::saveAdminNote,
            onDismiss   = viewModel::clearSelectedReport
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Summary Stats Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryStatsBanner(stats: AdminSummaryStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value    = stats.totalStudents.toString(),
            label    = "Students",
            icon     = Icons.Default.People,
            iconBg   = Color(0xFFE8E7FF),
            iconTint = AccentIndigo
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value    = stats.highRiskCount.toString(),
            label    = "High Risk",
            icon     = Icons.Default.Warning,
            iconBg   = RiskHighBg,
            iconTint = RiskHighFg
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value    = "${stats.avgWellness}%",
            label    = "Avg Wellness",
            icon     = Icons.Default.FavoriteBorder,
            iconBg   = RiskLowBg,
            iconTint = RiskLowFg
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value    = stats.lowAttendCount.toString(),
            label    = "Low Attend.",
            icon     = Icons.Default.EventBusy,
            iconBg   = RiskMedBg,
            iconTint = RiskMedFg
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Label1)
        Text(label, fontSize = 10.sp, color = Label2, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Search Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Label2, modifier = Modifier.size(18.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search student name or ID...", fontSize = 14.sp, color = Label2) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Label2, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Filter Chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(selected: ReportFilter, onSelect: (ReportFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ReportFilter.values()) { filter ->
            val isSelected = selected == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentIndigo else BgCard)
                    .border(
                        width = if (isSelected) 0.dp else 0.5.dp,
                        color = if (isSelected) Color.Transparent else Separator,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    filter.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Label2
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Report Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReportCard(report: WeeklyReport, onClick: () -> Unit) {
    val risk = report.riskLevel()
    val (riskBg, riskFg, riskLabel) = when (risk) {
        RiskLevel.HIGH   -> Triple(RiskHighBg, RiskHighFg, "High Risk")
        RiskLevel.MEDIUM -> Triple(RiskMedBg, RiskMedFg, "Medium Risk")
        RiskLevel.LOW    -> Triple(RiskLowBg, RiskLowFg, "Low Risk")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        report.studentName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(report.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Label1)
                    Text("${report.weekStartDate} – ${report.weekEndDate}", fontSize = 12.sp, color = Label2)
                }
            }

            // Risk badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(riskBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(riskLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = riskFg)
            }
        }

        Divider(color = Separator, thickness = 0.5.dp)

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MiniStat(
                label = "Attendance",
                value = "${report.attendancePercent.toInt()}%",
                valueColor = if (report.attendancePercent < 75f) RiskHighFg else RiskLowFg
            )
            MiniStat(
                label = "Avg Stress",
                value = String.format("%.1f", report.avgStressLevel),
                valueColor = if (report.avgStressLevel > 7f) RiskHighFg
                else if (report.avgStressLevel > 5f) RiskMedFg
                else RiskLowFg
            )
            MiniStat(
                label = "Meditation",
                value = "${report.totalMeditationMinutes}m",
                valueColor = AccentIndigo
            )
            MiniStat(
                label = "Wellness",
                value = "${report.wellnessScore}%",
                valueColor = AccentPurple
            )
        }

        // Admin note preview
        if (report.adminNote.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgField)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.StickyNote2, contentDescription = null, tint = Label2, modifier = Modifier.size(14.dp))
                Text(
                    report.adminNote,
                    fontSize = 12.sp,
                    color = Label2,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 11.sp, color = Label2)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Report Detail Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReportDetailDialog(
    report: WeeklyReport,
    adminNote: String,
    isSaving: Boolean,
    onNoteChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(report.studentName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Label1)
                    Text("${report.weekStartDate} – ${report.weekEndDate}", fontSize = 12.sp, color = Label2)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Label2)
                }
            }

            Divider(color = Separator, thickness = 0.5.dp)

            // Wellness score circle
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${report.wellnessScore}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AccentIndigo)
                        Text("score", fontSize = 10.sp, color = AccentIndigo)
                    }
                }
            }

            // Section: Attendance
            DetailSection(title = "Attendance") {
                DetailRow("Classes attended", "${report.presentDays}/${report.totalClasses}")
                DetailRow("Absent days", "${report.absentDays}", if (report.absentDays > 2) RiskHighFg else Label1)
                DetailRow("Late days", "${report.lateDays}")
                DetailRow("Attendance %", "${report.attendancePercent.toInt()}%",
                    if (report.attendancePercent < 75f) RiskHighFg else RiskLowFg)
            }

            // Section: Stress
            DetailSection(title = "Stress") {
                DetailRow("Avg level (1–10)", String.format("%.1f", report.avgStressLevel),
                    if (report.avgStressLevel > 7) RiskHighFg else RiskLowFg)
                DetailRow("Max level", "${report.maxStressLevel}")
                DetailRow("Check-ins", "${report.stressCheckInCount}")
                if (report.stressRiskFlag) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RiskHighBg)
                            .padding(8.dp)
                    ) {
                        Text("⚠ High stress detected this week", fontSize = 12.sp, color = RiskHighFg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Section: Meditation
            DetailSection(title = "Meditation") {
                DetailRow("Total minutes", "${report.totalMeditationMinutes} min")
                DetailRow("Sessions", "${report.meditationSessionCount}")
            }

            // Section: AI Chat
            DetailSection(title = "AI Chat") {
                DetailRow("Total messages", "${report.totalAiMessages}")
                DetailRow("Sessions", "${report.aiChatSessionCount}")
                if (report.dominantTopic.isNotBlank()) {
                    DetailRow("Main topic", report.dominantTopic.replaceFirstChar { it.uppercase() })
                }
            }

            Divider(color = Separator, thickness = 0.5.dp)

            // Admin note
            Text("Admin Note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Label1)
            OutlinedTextField(
                value = adminNote,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note about this student...", fontSize = 13.sp) },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AccentIndigo,
                    unfocusedBorderColor = Separator
                )
            )

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentIndigo)
                    .clickable(enabled = !isSaving) { onSaveNote() },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Note", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgField)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Label2, letterSpacing = 0.5.sp)
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Label1) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Label2)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = Label2, modifier = Modifier.size(48.dp))
            Text("No reports found", fontSize = 14.sp, color = Label2)
        }
    }
}