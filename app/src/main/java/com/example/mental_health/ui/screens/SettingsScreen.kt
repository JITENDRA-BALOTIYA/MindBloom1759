@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mental_health.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  Color System
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentGreen  = Color(0xFF34C759)
private val AccentAmber  = Color(0xFFFF9F0A)
private val AccentBlue   = Color(0xFF007AFF)
private val AccentRed    = Color(0xFFFF3B30)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Label3       = Color(0xFFC7C7CC)
private val Separator    = Color(0xFFE5E5EA)
private val BgField      = Color(0xFFF2F2F7)

// ─────────────────────────────────────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var dailyReminders  by remember { mutableStateOf(true) }
    var darkMode        by remember { mutableStateOf(false) }
    var wellnessTips    by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ──────────────────────────────────────────────────────
            SettingsTopBar(onBack = onBack)

            // ── Scrollable Content ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Profile Card ─────────────────────────────────────────────
                ProfileBannerCard()

                // ── Account ──────────────────────────────────────────────────
                SettingsGroup(label = "Account") {
                    SettingsNavRow(emoji = "👤", emojiColor = AccentIndigo,  title = "Profile Information")
                    SettingsDivider()
                    SettingsNavRow(emoji = "🔔", emojiColor = AccentAmber,   title = "Notifications")
                }

                // ── Privacy ───────────────────────────────────────────────────
                SettingsGroup(label = "Privacy") {
                    SettingsNavRow(emoji = "🔒", emojiColor = AccentGreen,  title = "Privacy & Security")
                    SettingsDivider()
                    SettingsNavRow(emoji = "🧹", emojiColor = AccentBlue,   title = "Clear App Data")
                }

                // ── Preferences ───────────────────────────────────────────────
                SettingsGroup(label = "Preferences") {
                    SettingsToggleRow(
                        emoji = "📅", emojiColor = AccentIndigo,
                        title = "Daily Reminders",
                        checked = dailyReminders,
                        onCheckedChange = { dailyReminders = it }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        emoji = "🌙", emojiColor = AccentPurple,
                        title = "Dark Mode",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        emoji = "❤️", emojiColor = AccentRed,
                        title = "Wellness Tips",
                        checked = wellnessTips,
                        onCheckedChange = { wellnessTips = it }
                    )
                }

                // ── Danger Zone ───────────────────────────────────────────────
                SettingsGroup(label = "") {
                    SettingsNavRow(
                        emoji = "🚪", emojiColor = AccentRed,
                        title = "Log Out",
                        titleColor = AccentRed,
                        showChevron = false
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsTopBar(onBack: () -> Unit) {
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
                        .background(AccentIndigo.copy(alpha = 0.10f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = AccentIndigo, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Settings", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Label1)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(34.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).align(Alignment.BottomCenter).background(Separator))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Profile Banner Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileBannerCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = AccentIndigo.copy(0.12f))
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentIndigo, AccentPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text("🧑", fontSize = 24.sp)
            }

            // Name + email
            Column(modifier = Modifier.weight(1f)) {
                Text("Rahul Sharma", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Label1)
                Text("rahul@example.com", fontSize = 13.sp, color = Label2, modifier = Modifier.padding(top = 2.dp))
            }

            // Edit button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentIndigo.copy(alpha = 0.08f))
                    .clickable { }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentIndigo)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Settings Group Container
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        if (label.isNotEmpty()) {
            Text(
                label.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Label2,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.05f))
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Navigation Row  (with chevron →)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsNavRow(
    emoji: String,
    emojiColor: Color,
    title: String,
    titleColor: Color = Label1,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Emoji icon box
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(emojiColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }

        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        if (showChevron) {
            Text("›", fontSize = 18.sp, color = Label3, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Toggle Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsToggleRow(
    emoji: String,
    emojiColor: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Emoji icon box
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(emojiColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }

        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Label1,
            modifier = Modifier.weight(1f)
        )

        // iOS-style Switch — using Material3 Switch with custom colors
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGreen,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Separator,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Divider
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(0.5.dp)
            .background(BgField)
    )
}