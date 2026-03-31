@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mental_health.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mental_health.ui.theme.DeepCoral
import com.example.mental_health.ui.theme.DeepMint

@Composable
fun AdminDashboardScreen(onBack: () -> Unit) {
    val students = listOf(
        StudentRisk("Rahul Sharma", "Moderate", 14, DeepCoral),
        StudentRisk("Priya Verma", "Low", 5, DeepMint),
        StudentRisk("Amit Patel", "High", 22, Color.Red),
        StudentRisk("Sneha Reddy", "Low", 3, DeepMint)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Student Risk Monitoring", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    StudentRiskCard(student)
                }
            }
        }
    }
}

data class StudentRisk(val name: String, val level: String, val score: Int, val color: Color)

@Composable
fun StudentRiskCard(student: StudentRisk) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("Stress Level: ${student.level}", color = student.color)
            }
            Box(
                modifier = Modifier
                    .background(student.color.copy(alpha = 0.1f), MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                Text(
                    text = student.score.toString(),
                    color = student.color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
