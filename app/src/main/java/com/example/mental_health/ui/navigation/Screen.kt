package com.example.mental_health.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object MoodCheck : Screen("mood_check")
    object AIChat : Screen("ai_chat")
    object RelaxMode : Screen("relax_mode")
    object PsychologyTest : Screen("psychology_test")
    object AdminDashboard : Screen("admin_dashboard")
    object Settings : Screen("settings")
    object FaceAttendance : Screen("face_attendance")
    object AttendanceHistory : Screen("attendance_history")
    object Meditation : Screen("meditation")
    object Profile : Screen("profile")
}
