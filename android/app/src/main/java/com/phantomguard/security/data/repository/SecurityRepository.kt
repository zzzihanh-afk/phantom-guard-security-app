package com.phantomguard.security.data.repository

import com.phantomguard.security.data.model.*
import com.phantomguard.security.domain.engine.ProtectionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityRepository {

    companion object {
        val instance: SecurityRepository by lazy { SecurityRepository() }
    }

    private val _currentUser = MutableStateFlow<User?>(
        User(
            uid = "usr_tanzimul_001",
            email = "tanzimul@phantomguard.security",
            displayName = "Tanzimul",
            role = UserRole.REGISTERED_ADMIN,
            deviceCount = 3
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _devices = MutableStateFlow<List<DeviceInfo>>(
        listOf(
            DeviceInfo(
                id = "dev_redmi_13",
                name = "Redmi Note 13",
                manufacturer = "Xiaomi",
                model = "23124RA7EO",
                androidVersion = "14",
                apiLevel = 34,
                batteryLevel = 85,
                isCharging = false,
                isOnline = true,
                protectionTier = ProtectionTier.DEEP_PROTECTION,
                lastSeen = "Just now",
                location = "Dhaka, Bangladesh",
                latitude = 23.8103,
                longitude = 90.4125
            ),
            DeviceInfo(
                id = "dev_samsung_a54",
                name = "Samsung A54",
                manufacturer = "Samsung",
                model = "SM-A546B",
                androidVersion = "14",
                apiLevel = 34,
                batteryLevel = 72,
                isCharging = true,
                isOnline = true,
                protectionTier = ProtectionTier.STANDARD_PROTECTION,
                lastSeen = "5 min ago",
                location = "Dhaka, Bangladesh",
                latitude = 23.7925,
                longitude = 90.4078
            ),
            DeviceInfo(
                id = "dev_realme_c67",
                name = "Realme C67",
                manufacturer = "Realme",
                model = "RMX3890",
                androidVersion = "13",
                apiLevel = 33,
                batteryLevel = 45,
                isCharging = false,
                isOnline = false,
                protectionTier = ProtectionTier.STANDARD_PROTECTION,
                lastSeen = "2h ago",
                location = "Dhaka, Bangladesh",
                latitude = 23.7771,
                longitude = 90.3994
            )
        )
    )
    val devices: StateFlow<List<DeviceInfo>> = _devices.asStateFlow()

    private val _alerts = MutableStateFlow<List<SecurityAlert>>(
        listOf(
            SecurityAlert(
                id = "alt_001",
                deviceId = "dev_redmi_13",
                deviceName = "Redmi Note 13",
                title = "SIM Change Detected",
                message = "New SIM detected on Redmi Note 13",
                severity = AlertSeverity.CRITICAL,
                timestamp = "10 Nov, 2026 • 09:24 AM"
            ),
            SecurityAlert(
                id = "alt_002",
                deviceId = "dev_realme_c67",
                deviceName = "Realme C67",
                title = "Device Offline",
                message = "Device is offline for more than 1 hour",
                severity = AlertSeverity.HIGH,
                timestamp = "10 Nov, 2026 • 08:15 AM"
            ),
            SecurityAlert(
                id = "alt_003",
                deviceId = "dev_redmi_13",
                deviceName = "Redmi Note 13",
                title = "Protection Active",
                message = "All systems are normal",
                severity = AlertSeverity.INFO,
                timestamp = "10 Nov, 2026 • 07:50 AM"
            )
        )
    )
    val alerts: StateFlow<List<SecurityAlert>> = _alerts.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg_001",
                sender = "Owner",
                isOwner = true,
                text = "Hello! This is the owner of this device. If you have found this phone, please contact me. Thank you!",
                timestamp = "10:14 AM"
            ),
            ChatMessage(
                id = "msg_002",
                sender = "Finder",
                isOwner = false,
                text = "Sure, I will return it. 👍",
                timestamp = "10:15 AM"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun switchRole(role: UserRole) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(role = role)
    }

    fun loginUser(email: String, name: String) {
        _currentUser.value = User(
            uid = "usr_" + System.currentTimeMillis(),
            email = email,
            displayName = name,
            role = UserRole.REGISTERED_ADMIN,
            deviceCount = _devices.value.size
        )
    }

    fun logout() {
        _currentUser.value = null
    }

    fun sendRecoveryMessage(text: String) {
        val newMsg = ChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            sender = "Owner",
            isOwner = true,
            text = text,
            timestamp = "Just now"
        )
        _chatMessages.value = _chatMessages.value + newMsg
    }

    fun addDevice(name: String, manufacturer: String, model: String) {
        val newDevice = DeviceInfo(
            id = "dev_" + System.currentTimeMillis(),
            name = name,
            manufacturer = manufacturer,
            model = model,
            androidVersion = "14",
            apiLevel = 34,
            batteryLevel = 100,
            isCharging = false,
            isOnline = true,
            protectionTier = ProtectionTier.DEEP_PROTECTION,
            lastSeen = "Just now"
        )
        _devices.value = _devices.value + newDevice
    }
}
