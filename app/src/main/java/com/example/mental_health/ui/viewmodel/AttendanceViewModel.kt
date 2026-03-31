package com.example.mental_health.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.AttendanceRecord
import com.example.mental_health.data.repository.AttendanceRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
// SEALED STATE
// ─────────────────────────────────────────────────────────────

sealed class AttendanceState {
    object Idle    : AttendanceState()
    object Loading : AttendanceState()
    object Success : AttendanceState()
    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.GENERAL
    ) : AttendanceState()
}

enum class ErrorType { TIME_WINDOW, LOCATION, PERMISSION, GENERAL }

// ─────────────────────────────────────────────────────────────
// CONFIG  ← change values here only
// NOTE: Time window is kept wide (8AM–6PM) for testing.
//       Change back to 9:00–9:30 for production.
// ─────────────────────────────────────────────────────────────

private object AttendanceConfig {
    // ── TIME WINDOW ──────────────────────────────────────────
    // For TESTING: 8AM to 6PM so you can test anytime
    // For PRODUCTION: Change to 9, 0, 9, 30
    const val WINDOW_START_HOUR   = 0
    const val WINDOW_START_MINUTE = 0
    const val WINDOW_END_HOUR     = 23
    const val WINDOW_END_MINUTE   = 59

    // ── COLLEGE LOCATION ──────────────────────────────────────
    // Replace with your actual college GPS coordinates
    const val COLLEGE_LAT      = 26.9124   // ← your college latitude
    const val COLLEGE_LNG      = 75.7873   // ← your college longitude
    const val ALLOWED_RADIUS_M = 200.0     // 200m radius (adjust as needed)

    // ── GOOGLE MAPS API KEY ───────────────────────────────────
    const val MAPS_API_KEY = "AIzaSyBbWWQLo2sjYq44pfbV2sneuiAIDghXQWk"
}

// ─────────────────────────────────────────────────────────────
// VIEW MODEL
// ─────────────────────────────────────────────────────────────

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    application: Application,
    private val repository: AttendanceRepository
) : AndroidViewModel(application) {

    private val _attendanceState = MutableStateFlow<AttendanceState>(AttendanceState.Idle)
    val attendanceState: StateFlow<AttendanceState> = _attendanceState.asStateFlow()

    private val _historyState = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val historyState: StateFlow<List<AttendanceRecord>> = _historyState.asStateFlow()

    // Exposes distance string for UI e.g. "48 m from college"
    private val _locationInfo = MutableStateFlow("")
    val locationInfo: StateFlow<String> = _locationInfo.asStateFlow()

    private val fusedLocation: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // ── CALLED FROM FaceAttendanceScreen after face detected ──
    fun markAttendance() {
        viewModelScope.launch {
            _attendanceState.value = AttendanceState.Loading

            // STEP 1: Check location permission explicitly
            if (!hasLocationPermission()) {
                _attendanceState.value = AttendanceState.Error(
                    message   = "Location permission not granted.\nPlease allow location access and try again.",
                    errorType = ErrorType.PERMISSION
                )
                return@launch
            }

            // STEP 2: Check time window
            val (timeOk, timeMsg) = checkTimeWindow()
            if (!timeOk) {
                _attendanceState.value = AttendanceState.Error(timeMsg, ErrorType.TIME_WINDOW)
                return@launch
            }

            // STEP 3: Get and verify geolocation
            val (locOk, locMsg) = checkLocation()
            if (!locOk) {
                val errorType = if (locMsg.contains("permission", ignoreCase = true))
                    ErrorType.PERMISSION else ErrorType.LOCATION
                _attendanceState.value = AttendanceState.Error(locMsg, errorType)
                return@launch
            }

            // STEP 4: All checks passed → save to repository
            repository.markAttendance()
                .onSuccess  { _attendanceState.value = AttendanceState.Success }
                .onFailure  {
                    _attendanceState.value = AttendanceState.Error(
                        it.message ?: "Failed to mark attendance. Please try again.",
                        ErrorType.GENERAL
                    )
                }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            repository.getAttendanceHistory()
                .onSuccess { _historyState.value = it }
        }
    }

    fun resetState() {
        _attendanceState.value = AttendanceState.Idle
        _locationInfo.value    = ""
    }

    // ─────────────────────────────────────────────────────────
    // PERMISSION CHECK
    // ─────────────────────────────────────────────────────────

    private fun hasLocationPermission(): Boolean {
        val ctx = getApplication<Application>()
        val fine = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    // ─────────────────────────────────────────────────────────
    // TIME WINDOW CHECK
    // ─────────────────────────────────────────────────────────

    private fun checkTimeWindow(): Pair<Boolean, String> {
        val cal   = Calendar.getInstance()
        val h     = cal.get(Calendar.HOUR_OF_DAY)
        val m     = cal.get(Calendar.MINUTE)
        val now   = h * 60 + m
        val start = AttendanceConfig.WINDOW_START_HOUR   * 60 + AttendanceConfig.WINDOW_START_MINUTE
        val end   = AttendanceConfig.WINDOW_END_HOUR     * 60 + AttendanceConfig.WINDOW_END_MINUTE

        return if (now in start..end) {
            true to "Within time window."
        } else {
            false to "Attendance window: ${fmt(AttendanceConfig.WINDOW_START_HOUR, AttendanceConfig.WINDOW_START_MINUTE)}" +
                    " – ${fmt(AttendanceConfig.WINDOW_END_HOUR, AttendanceConfig.WINDOW_END_MINUTE)}.\n" +
                    "Current time: ${fmt(h, m)}.\nPlease try within the allowed window."
        }
    }

    private fun fmt(h: Int, m: Int): String {
        val sfx = if (h < 12) "AM" else "PM"
        val hr  = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(hr, m, sfx)
    }

    // ─────────────────────────────────────────────────────────
    // LOCATION CHECK
    // ─────────────────────────────────────────────────────────

    private suspend fun checkLocation(): Pair<Boolean, String> {
        // Double-check permission before GPS fetch
        if (!hasLocationPermission()) {
            return false to "Location permission not granted or GPS unavailable.\nPlease enable location and try again."
        }

        val coords = getCoords()
            ?: return false to "Could not get your location.\nMake sure GPS is ON and try again."

        val (lat, lng) = coords
        val dist       = haversine(lat, lng, AttendanceConfig.COLLEGE_LAT, AttendanceConfig.COLLEGE_LNG)
        _locationInfo.value = "%.0f m from college".format(dist)

        val withinRange = dist <= AttendanceConfig.ALLOWED_RADIUS_M

        // Google Maps verification — falls back to true on any network error
        val mapsOk = runCatching { verifyWithMaps(lat, lng) }.getOrDefault(true)

        return if (withinRange && mapsOk) {
            true to "Location verified. You are ${dist.toInt()}m from college."
        } else {
            false to "You are ${dist.toInt()} m away from college.\n" +
                    "Must be within ${AttendanceConfig.ALLOWED_RADIUS_M.toInt()} m to mark attendance."
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET CURRENT GPS COORDINATES
    // Uses getCurrentLocation() which is more reliable than
    // lastLocation (lastLocation can be null/stale).
    // Permission is checked BEFORE calling this.
    // ─────────────────────────────────────────────────────────

    private suspend fun getCoords(): Pair<Double, Double>? {
        // Safety guard — never call without permission
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            try {
                @Suppress("MissingPermission") // Permission already verified above
                fusedLocation
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            cont.resume(loc.latitude to loc.longitude)
                        } else {
                            // getCurrentLocation returned null — try lastLocation as fallback
                            @Suppress("MissingPermission")
                            fusedLocation.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    cont.resume(
                                        if (lastLoc != null)
                                            lastLoc.latitude to lastLoc.longitude
                                        else null
                                    )
                                }
                                .addOnFailureListener { cont.resume(null) }
                        }
                    }
                    .addOnFailureListener { cont.resume(null) }

                cont.invokeOnCancellation { cts.cancel() }

            } catch (e: SecurityException) {
                // Should never reach here since we check permission above
                cont.resume(null)
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // HAVERSINE DISTANCE
    // ─────────────────────────────────────────────────────────

    private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R    = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a    = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ─────────────────────────────────────────────────────────
    // GOOGLE MAPS GEOCODING VALIDATION
    // Falls back to true on any network/API error so attendance
    // is never blocked purely due to Maps API outage.
    // ─────────────────────────────────────────────────────────

    private suspend fun verifyWithMaps(lat: Double, lng: Double): Boolean =
        suspendCancellableCoroutine { cont ->
            Thread {
                try {
                    val conn = (URL(
                        "https://maps.googleapis.com/maps/api/geocode/json" +
                                "?latlng=$lat,$lng&key=${AttendanceConfig.MAPS_API_KEY}"
                    ).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8_000
                        readTimeout    = 8_000
                    }
                    val body = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()

                    val json = JSONObject(body)
                    if (json.getString("status") != "OK") {
                        cont.resume(true) // API issue — don't block attendance
                        return@Thread
                    }

                    val addr = json.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("formatted_address")
                        .lowercase()

                    // Customize these keywords for your college city
                    val keywords = listOf("jaipur", "rajasthan")
                    cont.resume(keywords.any { it in addr })

                } catch (_: Exception) {
                    // Network error — fall back to haversine result only
                    cont.resume(true)
                }
            }.start()
        }
}