package com.phantomguard.security.domain.engine

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.phantomguard.security.receiver.PhantomDeviceAdminReceiver
import java.io.File

data class CompatibilityReport(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val isDeviceOwner: Boolean,
    val isDeviceAdmin: Boolean,
    val isRooted: Boolean,
    val bootloaderUnlocked: Boolean,
    val hasHardwareKeystore: Boolean,
    val hasStrongBox: Boolean,
    val hasBiometrics: Boolean,
    val hasLocationPermission: Boolean,
    val hasPhoneStatePermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isIgnoringBatteryOptimizations: Boolean,
    val canDrawOverlays: Boolean,
    val protectionTier: ProtectionTier,
    val supportedFeatures: List<FeatureStatus>,
    val oemRecommendations: List<String>
)

enum class ProtectionTier {
    DEEP_PROTECTION,     // Full Device Owner (DPC)
    STANDARD_PROTECTION, // Device Administrator
    BASIC_PROTECTION     // Regular APK Foreground Service
}

enum class FeatureAvailability {
    SUPPORTED,
    SUPPORTED_WITH_LIMITATIONS,
    REQUIRES_PERMISSION,
    REQUIRES_DEVICE_OWNER,
    REQUIRES_OEM_SUPPORT,
    UNSUPPORTED
}

enum class FeatureActionType {
    NONE,
    ACTIVATE_DEVICE_ADMIN,
    REQUEST_LOCATION_PERMISSION,
    REQUEST_PHONE_STATE_PERMISSION,
    REQUEST_NOTIFICATION_PERMISSION,
    REQUEST_OVERLAY_PERMISSION,
    REQUEST_BATTERY_OPTIMIZATION,
    VIEW_DEVICE_OWNER_GUIDE
}

data class FeatureStatus(
    val featureName: String,
    val availability: FeatureAvailability,
    val description: String,
    val actionType: FeatureActionType = FeatureActionType.NONE
)

class DeviceCompatibilityEngine(private val context: Context) {

    fun evaluateDevice(): CompatibilityReport {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, PhantomDeviceAdminReceiver::class.java)

        val isOwner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dpm.isDeviceOwnerApp(context.packageName)
        } else false

        val isAdmin = dpm.isAdminActive(adminComponent)
        val isRooted = checkRootHeuristics()
        val bootloaderUnlocked = checkBootloaderUnlocked()
        val hasStrongBox = checkStrongBoxSupport()
        val hasHardwareKeystore = checkHardwareKeystoreSupport()

        val hasLocation = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPhoneState = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else true

        val canOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        val tier = when {
            isOwner -> ProtectionTier.DEEP_PROTECTION
            isAdmin -> ProtectionTier.STANDARD_PROTECTION
            else -> ProtectionTier.BASIC_PROTECTION
        }

        val features = mutableListOf<FeatureStatus>()

        // 1. Remote Screen Lock (Requires Device Admin)
        features.add(
            FeatureStatus(
                featureName = "Remote Device Lock",
                availability = if (isAdmin || isOwner) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_PERMISSION,
                description = if (isAdmin || isOwner) "Instant hardware lock via DevicePolicyManager" else "Requires Device Administrator activation",
                actionType = if (isAdmin || isOwner) FeatureActionType.NONE else FeatureActionType.ACTIVATE_DEVICE_ADMIN
            )
        )

        // 2. Anti-Theft Siren Alarm
        features.add(
            FeatureStatus(
                featureName = "Emergency Alarm Siren",
                availability = FeatureAvailability.SUPPORTED,
                description = "Audible siren with audio stream override in foreground service",
                actionType = FeatureActionType.NONE
            )
        )

        // 3. Stolen Mode & Lock Task Mode
        features.add(
            FeatureStatus(
                featureName = "Stolen Mode Lockdown",
                availability = if (isOwner) FeatureAvailability.SUPPORTED else if (canOverlay) FeatureAvailability.SUPPORTED_WITH_LIMITATIONS else FeatureAvailability.REQUIRES_PERMISSION,
                description = if (isOwner) "Enterprise Lock Task Mode blocking status bar and gestures" else if (canOverlay) "Overlay lock screen and continuous tracking" else "Requires 'Display over other apps' permission",
                actionType = if (isOwner || canOverlay) FeatureActionType.NONE else FeatureActionType.REQUEST_OVERLAY_PERMISSION
            )
        )

        // 4. Uninstall Protection (Requires Device Owner)
        features.add(
            FeatureStatus(
                featureName = "Anti-Uninstall Protection",
                availability = if (isOwner) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_DEVICE_OWNER,
                description = if (isOwner) "Package uninstallation blocked by Device Owner policy" else "Requires Device Owner provisioning via ADB or QR",
                actionType = if (isOwner) FeatureActionType.NONE else FeatureActionType.VIEW_DEVICE_OWNER_GUIDE
            )
        )

        // 5. Factory Reset Protection (FRP)
        features.add(
            FeatureStatus(
                featureName = "Factory Reset Protection",
                availability = if (isOwner) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_DEVICE_OWNER,
                description = if (isOwner) "FRP bound to Master Admin Google Account" else "Requires Device Owner provisioning",
                actionType = if (isOwner) FeatureActionType.NONE else FeatureActionType.VIEW_DEVICE_OWNER_GUIDE
            )
        )

        // 6. SIM Change Detection
        features.add(
            FeatureStatus(
                featureName = "SIM Change Monitoring",
                availability = if (hasPhoneState) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_PERMISSION,
                description = if (hasPhoneState) "Detects SIM card insertion, swap, and carrier mismatch" else "Requires Phone State permission",
                actionType = if (hasPhoneState) FeatureActionType.NONE else FeatureActionType.REQUEST_PHONE_STATE_PERMISSION
            )
        )

        // 7. Live GPS Tracking
        features.add(
            FeatureStatus(
                featureName = "Live GPS Tracking",
                availability = if (hasLocation) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_PERMISSION,
                description = if (hasLocation) "Realtime GPS coordinates with accuracy rings" else "Requires Fine Location permission",
                actionType = if (hasLocation) FeatureActionType.NONE else FeatureActionType.REQUEST_LOCATION_PERMISSION
            )
        )

        // 8. Notification Alerts (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            features.add(
                FeatureStatus(
                    featureName = "Security Notification Alerts",
                    availability = if (hasNotification) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_PERMISSION,
                    description = if (hasNotification) "Realtime critical push alerts and foreground service notice" else "Requires Notification permission",
                    actionType = if (hasNotification) FeatureActionType.NONE else FeatureActionType.REQUEST_NOTIFICATION_PERMISSION
                )
            )
        }

        // 9. Battery Optimization Whitelist
        features.add(
            FeatureStatus(
                featureName = "Background 24/7 Surveillance",
                availability = if (isIgnoringBattery) FeatureAvailability.SUPPORTED else FeatureAvailability.REQUIRES_PERMISSION,
                description = if (isIgnoringBattery) "Unrestricted background execution without OS sleep kill" else "Requires battery optimization exemption",
                actionType = if (isIgnoringBattery) FeatureActionType.NONE else FeatureActionType.REQUEST_BATTERY_OPTIMIZATION
            )
        )

        // OEM specific optimizations
        val oemTips = generateOemRecommendations(Build.MANUFACTURER.lowercase())

        return CompatibilityReport(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            isDeviceOwner = isOwner,
            isDeviceAdmin = isAdmin,
            isRooted = isRooted,
            bootloaderUnlocked = bootloaderUnlocked,
            hasHardwareKeystore = hasHardwareKeystore,
            hasStrongBox = hasStrongBox,
            hasBiometrics = true,
            hasLocationPermission = hasLocation,
            hasPhoneStatePermission = hasPhoneState,
            hasNotificationPermission = hasNotification,
            isIgnoringBatteryOptimizations = isIgnoringBattery,
            canDrawOverlays = canOverlay,
            protectionTier = tier,
            supportedFeatures = features,
            oemRecommendations = oemTips
        )
    }

    private fun checkRootHeuristics(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkBootloaderUnlocked(): Boolean {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java)
            val bootloaderState = getMethod.invoke(null, "ro.boot.flash.locked") as? String
            bootloaderState == "0"
        } catch (_: Exception) {
            false
        }
    }

    private fun checkStrongBoxSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } else false
    }

    private fun checkHardwareKeystoreSupport(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun generateOemRecommendations(oem: String): List<String> {
        return when {
            oem.contains("xiaomi") || oem.contains("redmi") || oem.contains("poco") -> listOf(
                "Xiaomi MIUI/HyperOS: Enable Autostart in Security -> Manage Apps",
                "Grant 'Display pop-up windows while running in the background'",
                "Set Battery Saver to 'No Restrictions' for 24/7 theft surveillance"
            )
            oem.contains("samsung") -> listOf(
                "Samsung One UI: Add Phantom Guard to 'Never sleeping apps' in Device Care",
                "Lock the app in the recent apps menu to prevent background termination",
                "Enable 'Allow background activity' in App Info -> Battery"
            )
            oem.contains("oppo") || oem.contains("realme") || oem.contains("oneplus") -> listOf(
                "ColorOS / Realme UI / OxygenOS: Enable 'Allow auto-startup'",
                "Turn on 'Allow background activity' in Battery management"
            )
            oem.contains("vivo") -> listOf(
                "Vivo Funtouch: Whitelist in iManager -> App Manager -> Autostart",
                "Allow high background power consumption"
            )
            oem.contains("transsion") || oem.contains("tecno") || oem.contains("infinix") -> listOf(
                "Transsion HiOS/XOS: Enable in Phone Master -> Auto-start management",
                "Add to protected apps in Battery Lab"
            )
            oem.contains("huawei") -> listOf(
                "Huawei EMUI: In App Launch set to 'Manage Manually' (Enable Auto-launch & Run in background)"
            )
            else -> listOf(
                "Android Stock / Pixel: Disable battery optimization in App Info -> Battery"
            )
        }
    }
}
