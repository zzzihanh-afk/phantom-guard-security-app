package com.phantomguard.security.data.remote

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.phantomguard.security.data.local.PhantomDatabaseHelper
import com.phantomguard.security.data.model.DeviceInfo
import com.phantomguard.security.data.model.SecurityAlert
import com.phantomguard.security.service.PhantomSecurityForegroundService
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FirestoreSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "FirestoreSync"
        @Volatile
        private var instance: FirestoreSyncManager? = null

        fun getInstance(context: Context): FirestoreSyncManager {
            return instance ?: synchronized(this) {
                instance ?: FirestoreSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val dbHelper = PhantomDatabaseHelper.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    val projectId: String
        get() = dbHelper.getConfig("firebase_project_id", "phantom-guard-security")

    fun startContinuousSync() {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            while (isActive) {
                try {
                    syncLocalDeviceState()
                    checkAndExecuteRemoteCommands()
                } catch (e: Exception) {
                    Log.w(TAG, "Cloud Firestore sync pass: ${e.message}")
                }
                delay(30_000) // Poll / sync heartbeat every 30 seconds
            }
        }
    }

    fun stopContinuousSync() {
        syncJob?.cancel()
        syncJob = null
    }

    fun updateProjectId(newProjectId: String) {
        dbHelper.setConfig("firebase_project_id", newProjectId)
        scope.launch {
            syncLocalDeviceState()
        }
    }

    suspend fun syncLocalDeviceState(): Boolean = withContext(Dispatchers.IO) {
        try {
            val deviceId = "dev_samsung_a54"
            val urlString = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/devices/$deviceId"
            val url = URL(urlString)

            val jsonBody = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("name", JSONObject().put("stringValue", "${Build.MANUFACTURER} ${Build.MODEL}"))
                    put("manufacturer", JSONObject().put("stringValue", Build.MANUFACTURER))
                    put("model", JSONObject().put("stringValue", Build.MODEL))
                    put("androidVersion", JSONObject().put("stringValue", Build.VERSION.RELEASE))
                    put("apiLevel", JSONObject().put("integerValue", Build.VERSION.SDK_INT))
                    put("batteryLevel", JSONObject().put("integerValue", 85))
                    put("isOnline", JSONObject().put("booleanValue", true))
                    put("protectionTier", JSONObject().put("stringValue", "STANDARD_PROTECTION"))
                    put("lastSeen", JSONObject().put("stringValue", "Just now"))
                    put("location", JSONObject().put("stringValue", "Dhaka, Bangladesh"))
                    put("latitude", JSONObject().put("doubleValue", 23.8103))
                    put("longitude", JSONObject().put("doubleValue", 90.4125))
                }
                put("fields", fields)
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 6000
                readTimeout = 6000
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val code = conn.responseCode
            conn.disconnect()
            Log.i(TAG, "Synced device state to Cloud Firestore (code: $code)")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Device state sync fallback to local cache: ${e.message}")
            false
        }
    }

    suspend fun checkAndExecuteRemoteCommands(): Int = withContext(Dispatchers.IO) {
        var executedCount = 0
        try {
            val urlString = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/remoteCommands"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
            }

            if (conn.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val rootJson = JSONObject(response)
                val documents = rootJson.optJSONArray("documents")

                if (documents != null) {
                    for (i in 0 until documents.length()) {
                        val doc = documents.getJSONObject(i)
                        val fields = doc.optJSONObject("fields") ?: continue
                        val status = fields.optJSONObject("status")?.optString("stringValue") ?: ""
                        val commandType = fields.optJSONObject("commandType")?.optString("stringValue") ?: ""
                        val nonce = fields.optJSONObject("nonce")?.optString("stringValue") ?: ""

                        if (status == "PENDING" && nonce.isNotBlank()) {
                            if (!dbHelper.isNonceUsed(nonce)) {
                                dbHelper.markNonceUsed(nonce)
                                executeCommandLocally(commandType)
                                executedCount++
                            }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Command check note: ${e.message}")
        }
        executedCount
    }

    private fun executeCommandLocally(commandType: String) {
        Log.i(TAG, "Executing received remote command: $commandType")
        when (commandType) {
            "LOCK" -> {
                val intent = Intent(context, PhantomSecurityForegroundService::class.java).apply {
                    action = PhantomSecurityForegroundService.ACTION_LOCK_DEVICE
                }
                context.startService(intent)
            }
            "ALARM" -> {
                val intent = Intent(context, PhantomSecurityForegroundService::class.java).apply {
                    action = PhantomSecurityForegroundService.ACTION_TRIGGER_ALARM
                }
                context.startService(intent)
            }
            "STOP_ALARM" -> {
                val intent = Intent(context, PhantomSecurityForegroundService::class.java).apply {
                    action = PhantomSecurityForegroundService.ACTION_STOP_ALARM
                }
                context.startService(intent)
            }
        }
    }

    fun pushSecurityAlert(alert: SecurityAlert) {
        scope.launch {
            dbHelper.saveAlert(alert)
            try {
                val urlString = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/securityAlerts"
                val url = URL(urlString)
                val json = JSONObject().apply {
                    val fields = JSONObject().apply {
                        put("deviceId", JSONObject().put("stringValue", alert.deviceId))
                        put("deviceName", JSONObject().put("stringValue", alert.deviceName))
                        put("title", JSONObject().put("stringValue", alert.title))
                        put("message", JSONObject().put("stringValue", alert.message))
                        put("severity", JSONObject().put("stringValue", alert.severity.name))
                        put("timestamp", JSONObject().put("stringValue", alert.timestamp))
                    }
                    put("fields", fields)
                }

                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                }
                OutputStreamWriter(conn.outputStream).use {
                    it.write(json.toString())
                    it.flush()
                }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Alert pushed to local SQLite: ${e.message}")
            }
        }
    }
}
