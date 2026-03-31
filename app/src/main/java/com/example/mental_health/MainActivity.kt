package com.example.mental_health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mental_health.ui.navigation.Screen
import com.example.mental_health.ui.screens.*
import com.example.mental_health.ui.theme.Mental_HealthTheme
import com.example.mental_health.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mental_HealthTheme {
                MindBloomAppNavigation()
            }
        }
    }
}

@Composable
fun MindBloomAppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController, 
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNext = {
                val nextRoute = if (currentUser != null) Screen.Dashboard.route else Screen.Login.route
                navController.navigate(nextRoute) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { 
                    navController.navigate(Screen.Register.route) 
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { 
                    navController.popBackStack() 
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }
        
        composable(Screen.MoodCheck.route) { 
            MoodCheckScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMeditation = {
                    navController.navigate(Screen.Meditation.route) {
                        popUpTo(Screen.MoodCheck.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(Screen.AIChat.route) { 
            AIChatScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.RelaxMode.route) { 
            RelaxModeScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.PsychologyTest.route) { 
            PsychologyTestScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.AdminDashboard.route) { 
            AdminDashboardScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.Settings.route) { 
            SettingsScreen(onBack = { navController.popBackStack() }) 
        }

        composable(Screen.FaceAttendance.route) {
            FaceAttendanceScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AttendanceHistory.route) {
            AttendanceHistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Meditation.route) {
            MeditationScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
