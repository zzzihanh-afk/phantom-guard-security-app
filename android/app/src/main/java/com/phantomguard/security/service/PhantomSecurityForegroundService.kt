package com.phantomguard.security.service

import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phantomguard.security.MainActivity
import com.phantomguard.security.PhantomGuardApplication

class PhantomSecurityForegroundService : Service() {

    companion object {
        const val ACTION_START_PROTECTION = "ACTION_START_PROTECTION"
        const val ACTION_STOP_PROTECTION = "ACTION_STOP_PROTECTION"
        const val ACTION_TRIGGER_ALARM = "ACTION_TRIGGER_ALARM"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val ACTION_LOCK_DEVICE = "ACTION_LOCK_DEVICE"
        private const val NOTIFICATION_ID = 8881
        private const val TAG = "PhantomSecService"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isAlarmPlaying = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Phantom Guard Foreground Service created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithNotification()
        val action = intent?.action ?: ACTION_START_PROTECTION

        when (action) {
            ACTION_START_PROTECTION -> {
                // Foreground notification active
            }
            ACTION_STOP_PROTECTION -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TRIGGER_ALARM -> {
                triggerAudibleSiren()
            }
            ACTION_STOP_ALARM -> {
                stopAudibleSiren()
            }
            ACTION_LOCK_DEVICE -> {
                executeHardwareLock()
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, PhantomGuardApplication.CHANNEL_SECURITY_SERVICE)
            .setContentTitle("Phantom Guard Active")
            .setContentText("Continuous anti-theft & tamper surveillance active")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun triggerAudibleSiren() {
        if (isAlarmPlaying) return
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            isAlarmPlaying = true
            Log.i(TAG, "Emergency Siren activated at maximum volume.")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing siren alarm", e)
        }
    }

    private fun stopAudibleSiren() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        isAlarmPlaying = false
        Log.i(TAG, "Emergency Siren deactivated.")
    }

    private fun executeHardwareLock() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = android.content.ComponentName(this, com.phantomguard.security.receiver.PhantomDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(adminComponent)) {
            try {
                dpm.lockNow()
                Log.i(TAG, "Device lock executed successfully via DevicePolicyManager.")
            } catch (e: Exception) {
                Log.e(TAG, "Cannot lock device: ${e.message}", e)
            }
        } else {
            Log.w(TAG, "Cannot lock device: App is not active Device Administrator. Triggering warning siren.")
            triggerAudibleSiren()
        }
    }

    override fun onDestroy() {
        stopAudibleSiren()
        super.onDestroy()
    }
}
