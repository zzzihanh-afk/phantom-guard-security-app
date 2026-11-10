package com.phantomguard.security.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.phantomguard.security.data.model.*
import com.phantomguard.security.domain.engine.ProtectionTier

class PhantomDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "phantom_guard.db"
        const val DATABASE_VERSION = 1
        private var instance: PhantomDatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): PhantomDatabaseHelper {
            if (instance == null) {
                instance = PhantomDatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Table: Enrolled Devices
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS devices (
                id TEXT PRIMARY KEY,
                name TEXT,
                manufacturer TEXT,
                model TEXT,
                androidVersion TEXT,
                apiLevel INTEGER,
                batteryLevel INTEGER,
                isCharging INTEGER,
                isOnline INTEGER,
                protectionTier TEXT,
                lastSeen TEXT,
                location TEXT,
                latitude REAL,
                longitude REAL
            )
        """.trimIndent())

        // Table: Security Alerts
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS security_alerts (
                id TEXT PRIMARY KEY,
                deviceId TEXT,
                deviceName TEXT,
                title TEXT,
                message TEXT,
                severity TEXT,
                timestamp TEXT,
                isResolved INTEGER
            )
        """.trimIndent())

        // Table: Anti-Replay Nonces
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS executed_nonces (
                nonce TEXT PRIMARY KEY,
                executedAt INTEGER
            )
        """.trimIndent())

        // Table: Recovery Chat
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS recovery_chat (
                id TEXT PRIMARY KEY,
                sender TEXT,
                isOwner INTEGER,
                text TEXT,
                timestamp TEXT
            )
        """.trimIndent())

        // Table: Cloud Database Config
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cloud_config (
                config_key TEXT PRIMARY KEY,
                config_value TEXT
            )
        """.trimIndent())

        // Seed initial default config
        db.execSQL("INSERT OR REPLACE INTO cloud_config VALUES ('firebase_project_id', 'phantom-guard-security')")
        db.execSQL("INSERT OR REPLACE INTO cloud_config VALUES ('cloud_sync_enabled', '1')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS devices")
        db.execSQL("DROP TABLE IF EXISTS security_alerts")
        db.execSQL("DROP TABLE IF EXISTS executed_nonces")
        db.execSQL("DROP TABLE IF EXISTS recovery_chat")
        db.execSQL("DROP TABLE IF EXISTS cloud_config")
        onCreate(db)
    }

    fun saveDevice(device: DeviceInfo) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", device.id)
            put("name", device.name)
            put("manufacturer", device.manufacturer)
            put("model", device.model)
            put("androidVersion", device.androidVersion)
            put("apiLevel", device.apiLevel)
            put("batteryLevel", device.batteryLevel)
            put("isCharging", if (device.isCharging) 1 else 0)
            put("isOnline", if (device.isOnline) 1 else 0)
            put("protectionTier", device.protectionTier.name)
            put("lastSeen", device.lastSeen)
            put("location", device.location)
            put("latitude", device.latitude)
            put("longitude", device.longitude)
        }
        db.insertWithOnConflict("devices", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllDevices(): List<DeviceInfo> {
        val list = mutableListOf<DeviceInfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM devices", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val tierStr = c.getString(c.getColumnIndexOrThrow("protectionTier"))
                val tier = try { ProtectionTier.valueOf(tierStr) } catch (_: Exception) { ProtectionTier.STANDARD_PROTECTION }
                list.add(
                    DeviceInfo(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        name = c.getString(c.getColumnIndexOrThrow("name")),
                        manufacturer = c.getString(c.getColumnIndexOrThrow("manufacturer")),
                        model = c.getString(c.getColumnIndexOrThrow("model")),
                        androidVersion = c.getString(c.getColumnIndexOrThrow("androidVersion")),
                        apiLevel = c.getInt(c.getColumnIndexOrThrow("apiLevel")),
                        batteryLevel = c.getInt(c.getColumnIndexOrThrow("batteryLevel")),
                        isCharging = c.getInt(c.getColumnIndexOrThrow("isCharging")) == 1,
                        isOnline = c.getInt(c.getColumnIndexOrThrow("isOnline")) == 1,
                        protectionTier = tier,
                        lastSeen = c.getString(c.getColumnIndexOrThrow("lastSeen")),
                        location = c.getString(c.getColumnIndexOrThrow("location")),
                        latitude = c.getDouble(c.getColumnIndexOrThrow("latitude")),
                        longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"))
                    )
                )
            }
        }
        return list
    }

    fun saveAlert(alert: SecurityAlert) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", alert.id)
            put("deviceId", alert.deviceId)
            put("deviceName", alert.deviceName)
            put("title", alert.title)
            put("message", alert.message)
            put("severity", alert.severity.name)
            put("timestamp", alert.timestamp)
            put("isResolved", if (alert.isResolved) 1 else 0)
        }
        db.insertWithOnConflict("security_alerts", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllAlerts(): List<SecurityAlert> {
        val list = mutableListOf<SecurityAlert>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM security_alerts ORDER BY timestamp DESC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val sevStr = c.getString(c.getColumnIndexOrThrow("severity"))
                val severity = try { AlertSeverity.valueOf(sevStr) } catch (_: Exception) { AlertSeverity.INFO }
                list.add(
                    SecurityAlert(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        deviceId = c.getString(c.getColumnIndexOrThrow("deviceId")),
                        deviceName = c.getString(c.getColumnIndexOrThrow("deviceName")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        message = c.getString(c.getColumnIndexOrThrow("message")),
                        severity = severity,
                        timestamp = c.getString(c.getColumnIndexOrThrow("timestamp")),
                        isResolved = c.getInt(c.getColumnIndexOrThrow("isResolved")) == 1
                    )
                )
            }
        }
        return list
    }

    fun isNonceUsed(nonce: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM executed_nonces WHERE nonce = ?", arrayOf(nonce))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun markNonceUsed(nonce: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nonce", nonce)
            put("executedAt", System.currentTimeMillis())
        }
        db.insertWithOnConflict("executed_nonces", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getConfig(key: String, defaultValue: String = ""): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT config_value FROM cloud_config WHERE config_key = ?", arrayOf(key))
        cursor.use { c ->
            if (c.moveToFirst()) {
                return c.getString(0)
            }
        }
        return defaultValue
    }

    fun setConfig(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("config_key", key)
            put("config_value", value)
        }
        db.insertWithOnConflict("cloud_config", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
