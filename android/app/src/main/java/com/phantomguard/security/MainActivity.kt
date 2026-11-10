package com.phantomguard.security

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.phantomguard.security.data.remote.FirestoreSyncManager
import com.phantomguard.security.service.PhantomSecurityForegroundService
import com.phantomguard.security.ui.navigation.PhantomNavGraph
import com.phantomguard.security.ui.theme.PhantomGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirestoreSyncManager.getInstance(this).startContinuousSync()

        // Start Persistent Foreground Protection Service
        val serviceIntent = Intent(this, PhantomSecurityForegroundService::class.java).apply {
            action = PhantomSecurityForegroundService.ACTION_START_PROTECTION
        }
        try {
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Service start note: ${e.message}")
        }

        setContent {
            PhantomGuardTheme {
                PhantomNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreSyncManager.getInstance(this).startContinuousSync()
    }
}
