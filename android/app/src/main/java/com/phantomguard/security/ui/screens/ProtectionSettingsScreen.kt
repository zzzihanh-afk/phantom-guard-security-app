package com.phantomguard.security.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phantomguard.security.domain.engine.DeviceCompatibilityEngine
import com.phantomguard.security.domain.engine.ProtectionTier
import com.phantomguard.security.receiver.PhantomDeviceAdminReceiver
import com.phantomguard.security.ui.components.PhantomButton
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*

@Composable
fun ProtectionSettingsScreen(
    onBackClick: () -> Unit,
    onManageSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = remember { DeviceCompatibilityEngine(context) }
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

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshCounter++
    }

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

    val isDeepProtection = report.protectionTier == ProtectionTier.DEEP_PROTECTION
    val isStandardProtection = report.protectionTier == ProtectionTier.STANDARD_PROTECTION

    val protectionStatusColor = when {
        isDeepProtection -> SecurityGreen
        isStandardProtection -> NeonBlue
        else -> AlertAmber
    }

    val protectionStatusText = when {
        isDeepProtection -> "Deep Protection Active"
        isStandardProtection -> "Standard Protection Active"
        else -> "Basic Protection (Action Required)"
    }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Protection Settings",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                PhantomButton(
                    text = "Device Compatibility & Permission Hub",
                    onClick = onManageSettings
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Shield Status Graphic
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(protectionStatusColor.copy(alpha = 0.15f))
                    .border(2.dp, protectionStatusColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (report.isDeviceAdmin) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = protectionStatusColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = protectionStatusText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (report.isDeviceAdmin) "Hardware lock and security monitoring are active" else "Device Administrator is currently inactive",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Real Dynamic Checklist of Protections
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RealProtectionCheckItem(
                    title = "Remote Device Lock",
                    status = if (report.isDeviceAdmin) "Active" else "Requires Admin Activation",
                    isActive = report.isDeviceAdmin,
                    onClick = {
                        if (!report.isDeviceAdmin) launchDeviceAdmin() else onManageSettings()
                    }
                )

                RealProtectionCheckItem(
                    title = "Anti-Theft Siren Alarm",
                    status = "Ready (Max volume audio override)",
                    isActive = true,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "Live GPS Location Tracking",
                    status = if (report.hasLocationPermission) "Active" else "Requires Location Permission",
                    isActive = report.hasLocationPermission,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "SIM Card Change Monitoring",
                    status = if (report.hasPhoneStatePermission) "Active" else "Requires Phone State Permission",
                    isActive = report.hasPhoneStatePermission,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "Uninstall Protection",
                    status = if (report.isDeviceOwner) "Enforced by Device Owner" else "Requires Device Owner (ADB/QR)",
                    isActive = report.isDeviceOwner,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "Factory Reset Protection (FRP)",
                    status = if (report.isDeviceOwner) "Enforced by Device Owner" else "Requires Device Owner",
                    isActive = report.isDeviceOwner,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "Stolen Mode Lockdown",
                    status = if (report.canDrawOverlays || report.isDeviceOwner) "Ready" else "Requires Overlay Permission",
                    isActive = report.canDrawOverlays || report.isDeviceOwner,
                    onClick = onManageSettings
                )

                RealProtectionCheckItem(
                    title = "Boot Security & Tamper Detection",
                    status = if (report.bootloaderUnlocked) "Bootloader Unlocked Warning" else "Bootloader Locked Secure",
                    isActive = !report.bootloaderUnlocked,
                    onClick = onManageSettings
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RealProtectionCheckItem(
    title: String,
    status: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceNavy)
            .border(
                1.dp,
                if (!isActive) AlertAmber.copy(alpha = 0.5f) else SurfaceNavyBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isActive) SecurityGreen else AlertAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isActive) TextSecondary else AlertAmber,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (!isActive) {
                Text(
                    text = "Enable →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
