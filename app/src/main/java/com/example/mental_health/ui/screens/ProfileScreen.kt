package com.example.mental_health.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.mental_health.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
//  Apple Color System (matches DashboardScreen)
// ─────────────────────────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFFF2F2F7)
private val BgCard       = Color(0xFFFFFFFF)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentIndigoL= Color(0xFF7C7CFF)
private val AccentPurple = Color(0xFFAF52DE)
private val AccentRed    = Color(0xFFFF3B30)
private val AccentRedL   = Color(0xFFFF6B6B)
private val Label1       = Color(0xFF1C1C1E)
private val Label2       = Color(0xFF8E8E93)
private val Label3       = Color(0xFFC7C7CC)
private val Separator    = Color(0xFFF2F2F7)

// ─────────────────────────────────────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user      by viewModel.userState.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentIndigo)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AccentIndigo.copy(alpha = 0.10f))
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Student Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Label1
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.size(34.dp)) // balance
                }

                user?.let { student ->
                    // ── Hero card (gradient) ──────────────────────────────────
                    ProfileHeroCard(
                        name      = student.name,
                        college   = student.collegeName,
                        course    = student.course,
                        imageUrl  = student.profileImage
                    )

                    Spacer(Modifier.height(18.dp))

                    // ── Details list card ─────────────────────────────────────
                    val date = if (student.createdAt > 0)
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(student.createdAt))
                    else "N/A"

                    ProfileListCard(
                        items = listOf(
                            ProfileRow(Icons.Default.Person,       Color(0xFFEEF0FF), "Full Name",            student.name),
                            ProfileRow(Icons.Default.Cake,         Color(0xFFFFF3E8), "Age",                  student.age.toString()),
                            ProfileRow(Icons.Default.School,       Color(0xFFE8FFF4), "College",              student.collegeName),
                            ProfileRow(Icons.Default.Book,         Color(0xFFF3EEFF), "Course / Department",  student.course),
                            ProfileRow(Icons.Default.Email,        Color(0xFFEEF0FF), "Email",                student.email),
                            ProfileRow(Icons.Default.Phone,        Color(0xFFFFECEE), "Parent Phone Number",  student.parentPhone),
                            ProfileRow(Icons.Default.CalendarToday,Color(0xFFE8FFF4), "Registration Date",    date),
                        )
                    )

                    Spacer(Modifier.height(18.dp))

                    // ── Action buttons ────────────────────────────────────────
                    ProfileActions(
                        onEdit   = { /* TODO */ },
                        onLogout = { viewModel.logout(); onLogout() }
                    )

                    Spacer(Modifier.height(48.dp))

                } ?: run {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unable to load profile", color = Label2)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Hero Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileHeroCard(name: String, college: String, course: String, imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(14.dp, RoundedCornerShape(28.dp), spotColor = AccentIndigo.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL, AccentPurple)))
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                if (imageUrl.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFFE3F0FF), Color(0xFFD1E8FF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentIndigo,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(college, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))

            Spacer(Modifier.height(12.dp))

            // Course badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    "🎓  $course",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Details List Card (grouped iOS settings style)
// ─────────────────────────────────────────────────────────────────────────────
data class ProfileRow(
    val icon: ImageVector,
    val iconBg: Color,
    val label: String,
    val value: String
)

@Composable
fun ProfileListCard(items: List<ProfileRow>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
    ) {
        Column {
            items.forEachIndexed { index, row ->
                ProfileRowItem(row = row)
                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .padding(start = 70.dp)
                            .background(Separator)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileRowItem(row: ProfileRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(row.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(row.icon, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(row.label, fontSize = 11.sp, color = Label2, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(1.dp))
            Text(row.value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Label1)
        }

        // iOS disclosure chevron
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Label3,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Action Buttons
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileActions(onEdit: () -> Unit, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Edit Profile — outlined
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                .clip(RoundedCornerShape(16.dp))
                .background(BgCard)
                .border(1.5.dp, AccentIndigo, RoundedCornerShape(16.dp))
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(17.dp))
                Text("Edit Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentIndigo)
            }
        }

        // Logout — gradient red
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = AccentRed.copy(alpha = 0.30f))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(AccentRed, AccentRedL)))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                Text("Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}