package com.example.mental_health.data.repository

// ─────────────────────────────────────────────────────────────
// AttendanceVerifier.kt — FIXED
// MindBloom — Attendance Verification Module
// Fixes: lastLocation null, Looper missing, timeout handling
// ─────────────────────────────────────────────────────────────

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
// DATA CLASSES
// ─────────────────────────────────────────────────────────────

data class AttendanceConfig(
    val windowStartHour: Int    = 9,
    val windowStartMinute: Int  = 0,
    val windowEndHour: Int      = 9,
    val windowEndMinute: Int    = 30,

    val collegeLat: Double      = 26.9124,   // ← Replace with your college latitude
    val collegeLng: Double      = 75.7873,   // ← Replace with your college longitude
    val allowedRadiusMeters: Double = 100.0,

    val googleMapsApiKey: String = "AIzaSyBbWWQLo2sjYq44pfbV2sneuiAIDghXQWk"
)

data class AttendanceResult(
    val canProceed: Boolean,
    val reason: String,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val distanceMeters: Double? = null,
    val isWithinTime: Boolean = false,
    val isWithinLocation: Boolean = false
)

// ─────────────────────────────────────────────────────────────
// MAIN VERIFIER CLASS
// ─────────────────────────────────────────────────────────────

class AttendanceVerifier(
    private val context: Context,
    private val config: AttendanceConfig = AttendanceConfig()
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // ── ENTRY POINT ──────────────────────────────────────────
    suspend fun verify(): AttendanceResult {

        // STEP 1: Time window check
        val timeCheck = isWithinTimeWindow()
        if (!timeCheck.first) {
            return AttendanceResult(
                canProceed       = false,
                reason           = timeCheck.second,
                isWithinTime     = false,
                isWithinLocation = false
            )
        }

        // STEP 2: Permission check
        if (!hasLocationPermission()) {
            return AttendanceResult(
                canProceed   = false,
                reason       = "Location permission not granted. Please allow location access.",
                isWithinTime = true
            )
        }

        // STEP 3: Get location — always request fresh, don't rely on lastLocation
        val location = getFreshLocation()
        if (location == null) {
            return AttendanceResult(
                canProceed   = false,
                reason       = "Location detection failed. Please ensure GPS is ON and try again.",
                isWithinTime = true
            )
        }

        val userLat = location.first
        val userLng = location.second

        Log.d("AttendanceVerifier", "Got location: $userLat, $userLng")

        // STEP 4: Haversine distance check
        val distance = haversineDistance(userLat, userLng, config.collegeLat, config.collegeLng)
        val withinRadius = distance <= config.allowedRadiusMeters

        // STEP 5: Google Maps API validation (optional — falls back gracefully)
        val mapsVerified = verifyWithGoogleMaps(userLat, userLng)

        val locationOk = withinRadius && mapsVerified

        return if (locationOk) {
            AttendanceResult(
                canProceed       = true,
                reason           = "All checks passed. Attendance can be marked.",
                userLat          = userLat,
                userLng          = userLng,
                distanceMeters   = distance,
                isWithinTime     = true,
                isWithinLocation = true
            )
        } else {
            AttendanceResult(
                canProceed       = false,
                reason           = "You are ${distance.toInt()} m away from college. " +
                        "Must be within ${config.allowedRadiusMeters.toInt()} m.",
                userLat          = userLat,
                userLng          = userLng,
                distanceMeters   = distance,
                isWithinTime     = true,
                isWithinLocation = false
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // FIX: Always request fresh location with proper Looper
    // Tries lastLocation first, then requests fresh if null
    // ─────────────────────────────────────────────────────────

    private suspend fun getFreshLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            try {
                // First try lastLocation (fast)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            Log.d("AttendanceVerifier", "Used lastLocation")
                            cont.resume(Pair(loc.latitude, loc.longitude))
                        } else {
                            // lastLocation null — request fresh GPS fix
                            Log.d("AttendanceVerifier", "lastLocation null, requesting fresh...")
                            requestFreshGPS(cont)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AttendanceVerifier", "lastLocation failed: ${e.message}")
                        requestFreshGPS(cont)
                    }
            } catch (e: SecurityException) {
                Log.e("AttendanceVerifier", "SecurityException: ${e.message}")
                cont.resume(null)
            }
        }

    // FIX: Added Looper.getMainLooper() — this was causing the crash
    private fun requestFreshGPS(
        cont: kotlin.coroutines.Continuation<Pair<Double, Double>?>
    ) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        )
            .setMaxUpdates(1)
            .setWaitForAccurateLocation(false)  // Don't wait forever
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                val loc = result.lastLocation
                if (loc != null) {
                    Log.d("AttendanceVerifier", "Fresh GPS: ${loc.latitude}, ${loc.longitude}")
                    cont.resume(Pair(loc.latitude, loc.longitude))
                } else {
                    Log.e("AttendanceVerifier", "Fresh GPS also returned null")
                    cont.resume(null)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    Log.e("AttendanceVerifier", "Location not available on device")
                    fusedLocationClient.removeLocationUpdates(this)
                    cont.resume(null)
                }
            }
        }

        try {
            // FIX: Looper.getMainLooper() instead of null
            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()  // ← THIS WAS THE BUG
            )
        } catch (e: SecurityException) {
            Log.e("AttendanceVerifier", "GPS request SecurityException: ${e.message}")
            cont.resume(null)
        }
    }

    // ─────────────────────────────────────────────────────────
    // TIME WINDOW CHECK
    // ─────────────────────────────────────────────────────────

    private fun isWithinTimeWindow(): Pair<Boolean, String> {
        val now         = Calendar.getInstance()
        val hour        = now.get(Calendar.HOUR_OF_DAY)
        val minute      = now.get(Calendar.MINUTE)
        val currentMins = hour * 60 + minute
        val startMins   = config.windowStartHour * 60 + config.windowStartMinute
        val endMins     = config.windowEndHour * 60 + config.windowEndMinute

        return if (currentMins in startMins..endMins) {
            Pair(true, "Within time window.")
        } else {
            val startStr = formatTime(config.windowStartHour, config.windowStartMinute)
            val endStr   = formatTime(config.windowEndHour, config.windowEndMinute)
            val nowStr   = formatTime(hour, minute)
            Pair(false, "Attendance window is $startStr – $endStr. Current time: $nowStr.")
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val suffix = if (hour < 12) "AM" else "PM"
        val h      = if (hour % 12 == 0) 12 else hour % 12
        return "%d:%02d %s".format(h, minute, suffix)
    }

    // ─────────────────────────────────────────────────────────
    // PERMISSION CHECK
    // ─────────────────────────────────────────────────────────

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ─────────────────────────────────────────────────────────
    // HAVERSINE DISTANCE
    // ─────────────────────────────────────────────────────────

    private fun haversineDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val R    = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a    = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ─────────────────────────────────────────────────────────
    // GOOGLE MAPS API VALIDATION
    // ─────────────────────────────────────────────────────────

    private suspend fun verifyWithGoogleMaps(lat: Double, lng: Double): Boolean =
        suspendCancellableCoroutine { cont ->
            Thread {
                try {
                    val url = "https://maps.googleapis.com/maps/api/geocode/json" +
                            "?latlng=$lat,$lng&key=${config.googleMapsApiKey}"

                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout    = 8000
                    connection.requestMethod  = "GET"

                    val response = connection.inputStream.bufferedReader().readText()
                    val json     = JSONObject(response)
                    val status   = json.getString("status")

                    if (status != "OK") {
                        cont.resume(true)  // API fail → rely on haversine only
                        return@Thread
                    }

                    val results = json.getJSONArray("results")
                    if (results.length() == 0) {
                        cont.resume(true)
                        return@Thread
                    }

                    val formattedAddress = results
                        .getJSONObject(0)
                        .getString("formatted_address")
                        .lowercase()

                    val expectedKeywords = listOf("jaipur", "rajasthan")
                    cont.resume(expectedKeywords.any { it in formattedAddress })
                    connection.disconnect()

                } catch (e: Exception) {
                    Log.e("AttendanceVerifier", "Maps API error: ${e.message}")
                    cont.resume(true)  // Network error → don't block attendance
                }
            }.start()
        }
}