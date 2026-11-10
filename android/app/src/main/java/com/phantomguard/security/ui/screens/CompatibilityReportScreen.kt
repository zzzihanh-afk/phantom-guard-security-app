package com.phantomguard.security.ui.screens

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phantomguard.security.domain.engine.*
import com.phantomguard.security.receiver.PhantomDeviceAdminReceiver
import com.phantomguard.security.ui.components.PhantomButton
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*

@Composable
fun CompatibilityReportScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = remember { DeviceCompatibilityEngine(context) }

    // Trigger state to refresh the report whenever user returns from settings or dialogs
    var refreshCounter by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshCounter++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val report = remember(refreshCounter) { engine.evaluateDevice() }
    var showDeviceOwnerDialog by remember { mutableStateOf(false) }

    // Launchers for system permissions & Device Admin
    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshCounter++
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshCounter++
    }

    val phoneStatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshCounter++
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshCounter++
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshCounter++
    }

    val batteryOptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshCounter++
    }

    // Helper functions to launch permissions
    val launchDeviceAdmin = {
        val adminComponent = ComponentName(context, PhantomDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Phantom Guard requires Device Administrator privileges for remote hardware lock, wipe, and anti-theft security."
            )
        }
        deviceAdminLauncher.launch(intent)
    }

    val launchLocationPermission = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val launchPhonePermission = {
        phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
    }

    val launchNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launchOverlayPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    val launchBatteryOpt = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            batteryOptLauncher.launch(intent)
        }
    }

    val grantAllPermissions = {
        if (!report.hasLocationPermission) {
            launchLocationPermission()
        } else if (!report.hasPhoneStatePermission) {
            launchPhonePermission()
        } else if (!report.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launchNotificationPermission()
        } else if (!report.isDeviceAdmin) {
            launchDeviceAdmin()
        } else if (!report.isIgnoringBatteryOptimizations) {
            launchBatteryOpt()
        } else if (!report.canDrawOverlays) {
            launchOverlayPermission()
        } else {
            Toast.makeText(context, "All standard permissions are already granted!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Device Compatibility",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { refreshCounter++ }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyanGlow
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Floating Quick Action: Grant All Permissions
            val hasPendingPermissions = !report.isDeviceAdmin ||
                    !report.hasLocationPermission ||
                    !report.hasPhoneStatePermission ||
                    (!report.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ||
                    !report.isIgnoringBatteryOptimizations

            if (hasPendingPermissions) {
                Surface(
                    color = SurfaceNavy,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PhantomButton(
                            text = "Grant All Permissions & Activate",
                            icon = Icons.Default.CheckCircle,
                            onClick = grantAllPermissions
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hardware & OS Specs Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${report.manufacturer} ${report.model}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Android ${report.androidVersion} (API ${report.apiLevel})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanGlow)
                                )
                            }

                            val (badgeText, badgeColor) = when (report.protectionTier) {
                                ProtectionTier.DEEP_PROTECTION -> "Deep Protection" to SecurityGreen
                                ProtectionTier.STANDARD_PROTECTION -> "Standard" to NeonBlue
                                ProtectionTier.BASIC_PROTECTION -> "Basic" to AlertAmber
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = SurfaceNavyBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecItem("Device Owner", if (report.isDeviceOwner) "ACTIVE" else "NOT PROVISIONED", if (report.isDeviceOwner) SecurityGreen else TextSecondary)
                            SpecItem("Device Admin", if (report.isDeviceAdmin) "ACTIVE" else "INACTIVE", if (report.isDeviceAdmin) SecurityGreen else EmergencyRed)
                            SpecItem("Rooted", if (report.isRooted) "DETECTED" else "CLEAN", if (report.isRooted) EmergencyRed else SecurityGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecItem("Bootloader", if (report.bootloaderUnlocked) "UNLOCKED" else "LOCKED", if (report.bootloaderUnlocked) AlertAmber else SecurityGreen)
                            SpecItem("Keystore", if (report.hasHardwareKeystore) "HARDWARE" else "SOFTWARE", CyanGlow)
                            SpecItem("Security Patch", report.securityPatch, TextSecondary)
                        }
                    }
                }
            }

            // Quick Device Administrator Activation Banner (if inactive)
            if (!report.isDeviceAdmin) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceNavyElevated)
                            .border(1.5.dp, CyanGlow, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Device Administrator Required",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Activate Phantom Guard as Device Administrator to enable Remote Screen Lock, Wipe Data, and Unauthorized Unlock Detection.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = launchDeviceAdmin,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Activate Device Administrator", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section Header: Universal Compatibility Engine
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Feature Compatibility Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Tap to grant",
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow)
                    )
                }
            }

            // Feature List Items with Direct Interactive Actions
            items(report.supportedFeatures) { feature ->
                val isClickable = feature.actionType != FeatureActionType.NONE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceNavy)
                        .border(
                            1.dp,
                            if (isClickable) CyanGlow.copy(alpha = 0.4f) else SurfaceNavyBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = isClickable) {
                            when (feature.actionType) {
                                FeatureActionType.ACTIVATE_DEVICE_ADMIN -> launchDeviceAdmin()
                                FeatureActionType.REQUEST_LOCATION_PERMISSION -> launchLocationPermission()
                                FeatureActionType.REQUEST_PHONE_STATE_PERMISSION -> launchPhonePermission()
                                FeatureActionType.REQUEST_NOTIFICATION_PERMISSION -> launchNotificationPermission()
                                FeatureActionType.REQUEST_OVERLAY_PERMISSION -> launchOverlayPermission()
                                FeatureActionType.REQUEST_BATTERY_OPTIMIZATION -> launchBatteryOpt()
                                FeatureActionType.VIEW_DEVICE_OWNER_GUIDE -> showDeviceOwnerDialog = true
                                FeatureActionType.NONE -> {}
                            }
                        }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = feature.featureName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )

                            val (statusText, statusColor) = when (feature.availability) {
                                FeatureAvailability.SUPPORTED -> "Supported" to SecurityGreen
                                FeatureAvailability.SUPPORTED_WITH_LIMITATIONS -> "Limited" to AlertAmber
                                FeatureAvailability.REQUIRES_PERMISSION -> "Requires Permission" to CyanAccent
                                FeatureAvailability.REQUIRES_DEVICE_OWNER -> "Requires Device Owner" to Color(0xFFA855F7)
                                FeatureAvailability.REQUIRES_OEM_SUPPORT -> "Requires OEM" to AlertAmber
                                FeatureAvailability.UNSUPPORTED -> "Unsupported" to EmergencyRed
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )

                        // Interactive Action Button on the card
                        if (isClickable) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val actionLabel = when (feature.actionType) {
                                FeatureActionType.ACTIVATE_DEVICE_ADMIN -> "Activate Device Admin →"
                                FeatureActionType.REQUEST_LOCATION_PERMISSION -> "Grant Location Permission →"
                                FeatureActionType.REQUEST_PHONE_STATE_PERMISSION -> "Grant Phone State Permission →"
                                FeatureActionType.REQUEST_NOTIFICATION_PERMISSION -> "Allow Notifications →"
                                FeatureActionType.REQUEST_OVERLAY_PERMISSION -> "Allow Display Over Apps →"
                                FeatureActionType.REQUEST_BATTERY_OPTIMIZATION -> "Disable Battery Optimization →"
                                FeatureActionType.VIEW_DEVICE_OWNER_GUIDE -> "Device Owner Setup (ADB / QR) →"
                                FeatureActionType.NONE -> ""
                            }

                            val actionBtnColor = when (feature.actionType) {
                                FeatureActionType.VIEW_DEVICE_OWNER_GUIDE -> Color(0xFFA855F7)
                                else -> CyanGlow
                            }

                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = actionBtnColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // OEM Optimization Recommendations
            if (report.oemRecommendations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "OEM Power & Autostart Optimization",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                items(report.oemRecommendations) { tip ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceNavyElevated)
                            .border(1.dp, CyanGlow.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Device Owner Setup Guide Dialog with One-Click Copy
        if (showDeviceOwnerDialog) {
            val adbCommand = "adb shell dpm set-device-owner ${context.packageName}/com.phantomguard.security.receiver.PhantomDeviceAdminReceiver"

            AlertDialog(
                onDismissRequest = { showDeviceOwnerDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device Owner Provisioning", color = TextPrimary)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Android Enterprise Device Owner grants Deep Protection: Anti-Uninstall and Factory Reset Protection (FRP).",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Option 1: ADB Command (Development & Testing)",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkNavyBg)
                                .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = adbCommand,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ADB Command", adbCommand)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ADB command copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavyElevated)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy ADB Command", color = CyanGlow, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Option 2: Zero-Touch QR Code",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "On a factory reset device, tap 6 times on the welcome screen to scan the Phantom Guard enrollment QR code.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDeviceOwnerDialog = false }) {
                        Text("Done", color = CyanGlow, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceNavy
            )
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 12.sp
            )
        )
    }
}
