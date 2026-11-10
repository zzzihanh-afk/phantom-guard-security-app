package com.phantomguard.security

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.phantomguard.security.data.local.PhantomDatabaseHelper
import com.phantomguard.security.data.remote.FirestoreSyncManager

class PhantomGuardApplication : Application() {

    companion object {
        const val CHANNEL_SECURITY_SERVICE = "phantom_security_service"
        const val CHANNEL_ALERTS = "phantom_security_alerts"
        const val CHANNEL_COMMANDS = "phantom_remote_commands"
        lateinit var instance: PhantomGuardApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        
        // Initialize persistent SQLite database & start Cloud Firestore real-time synchronization
        PhantomDatabaseHelper.getInstance(this)
        FirestoreSyncManager.getInstance(this).startContinuousSync()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Background Service channel (silent, low disturbance)
            val serviceChannel = NotificationChannel(
                CHANNEL_SECURITY_SERVICE,
                "Phantom Guard Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Phantom Guard active for real-time theft prevention"
                setShowBadge(false)
            }

            // High Priority Security Alerts channel
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Phantom Guard Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts: SIM tampering, unauthorized unlock, and security warnings"
                enableVibration(true)
                setShowBadge(true)
            }

            // Remote Command Execution channel
            val commandsChannel = NotificationChannel(
                CHANNEL_COMMANDS,
                "Remote Command Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Status of remote locks, sirens, and anti-theft commands"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(serviceChannel, alertsChannel, commandsChannel))
        }
    }
}
