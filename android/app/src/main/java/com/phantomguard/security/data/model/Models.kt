package com.phantomguard.security.data.model

import com.phantomguard.security.domain.engine.ProtectionTier

enum class UserRole {
    REGISTERED_ADMIN,
    GLOBAL_MASTER_ADMIN
}

data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val isSuspended: Boolean = false,
    val deviceCount: Int = 0
)

data class DeviceInfo(
    val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean,
    val protectionTier: ProtectionTier,
    val lastSeen: String,
    val location: String = "Dhaka, Bangladesh",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125
)

enum class AlertSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    INFO
}

data class SecurityAlert(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: String,
    val isResolved: Boolean = false
)

enum class RemoteCommandType {
    LOCK_DEVICE,
    ALARM,
    STOLEN_MODE,
    LOCATE,
    SEND_MESSAGE,
    WIPE_DATA,
    EMERGENCY_LOCKDOWN
}

enum class CommandStatus {
    PENDING,
    EXECUTED,
    FAILED,
    REJECTED
}

data class RemoteCommand(
    val id: String,
    val deviceId: String,
    val commandType: RemoteCommandType,
    val timestamp: Long,
    val status: CommandStatus,
    val payload: Map<String, String> = emptyMap(),
    val resultMessage: String? = null
)

data class ChatMessage(
    val id: String,
    val sender: String,
    val isOwner: Boolean,
    val text: String,
    val timestamp: String
)
