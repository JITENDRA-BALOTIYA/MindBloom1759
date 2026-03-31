package com.example.mental_health

import android.app.Application
import android.content.Intent
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MindBloomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Firebase
        FirebaseApp.initializeApp(this)

        // 2. Initialize App Check to fix the "No AppCheckProvider installed" error spammed in logs.
        // This ensures Firebase services (Database, Auth) work smoothly without waiting for placeholder tokens.
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // 3. Fix SSL/TLS networking issues (Broken pipe/Connection reset) asynchronously.
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstalled() {
                // Provider is updated
            }

            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
                // Handle failure
            }
        })
    }
}
