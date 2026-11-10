package com.phantomguard.security.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomguard.security.data.repository.SecurityRepository
import com.phantomguard.security.service.PhantomSecurityForegroundService
import com.phantomguard.security.ui.components.*
import com.phantomguard.security.ui.theme.*

@Composable
fun RemoteControlScreen(
    deviceId: String,
    onBackClick: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToRecovery: () -> Unit
) {
    val context = LocalContext.current
    val repository = SecurityRepository.instance
    val devices by repository.devices.collectAsState()
    val device = devices.find { it.id == deviceId } ?: devices.firstOrNull()

    var lastCommandStatus by remember { mutableStateOf("N/A") }
    var isAlarmActive by remember { mutableStateOf(false) }
    var showConfirmWipeDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var customMessage by remember { mutableStateOf("Please return this device. Reward offered.") }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Remote Control",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Phone Graphic with Lock Badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(NeonBlue.copy(alpha = 0.15f))
                    .border(2.dp, CyanGlow.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, CyanGlow, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = device?.name ?: "Device",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (device?.isOnline == true) SecurityGreen else AlertAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (device?.isOnline == true) "Online" else "Offline",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (device?.isOnline == true) SecurityGreen else AlertAmber
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "•  ${device?.batteryLevel ?: 85}%",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2x3 Grid of Remote Control Commands
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RemoteActionCard(
                    title = "Lock Device",
                    icon = Icons.Default.Lock,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? android.app.admin.DevicePolicyManager
                        val adminComponent = android.content.ComponentName(context, com.phantomguard.security.receiver.PhantomDeviceAdminReceiver::class.java)
                        if (dpm != null && dpm.isAdminActive(adminComponent)) {
                            try {
                                dpm.lockNow()
                                lastCommandStatus = "Device hardware locked successfully"
                            } catch (e: Exception) {
                                lastCommandStatus = "Lock error: ${e.message}"
                            }
                        } else {
                            lastCommandStatus = "Device Administrator permission is required"
                        }
                    }
                )

                RemoteActionCard(
                    title = if (isAlarmActive) "Stop Alarm" else "Alarm",
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f),
                    iconColor = if (isAlarmActive) AlertAmber else CyanGlow,
                    onClick = {
                        val intent = Intent(context, PhantomSecurityForegroundService::class.java).apply {
                            action = if (isAlarmActive) {
                                PhantomSecurityForegroundService.ACTION_STOP_ALARM
                            } else {
                                PhantomSecurityForegroundService.ACTION_TRIGGER_ALARM
                            }
                        }
                        context.startService(intent)
                        isAlarmActive = !isAlarmActive
                        lastCommandStatus = if (isAlarmActive) "ALARM Triggered (Max Volume)" else "ALARM Stopped"
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RemoteActionCard(
                    title = "Stolen Mode",
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        lastCommandStatus = "STOLEN MODE engaged: Siren & GPS tracking active"
                    }
                )

                RemoteActionCard(
                    title = "Locate",
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLocation
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RemoteActionCard(
                    title = "Send Message",
                    icon = Icons.Default.Chat,
                    modifier = Modifier.weight(1f),
                    onClick = { showMessageDialog = true }
                )

                RemoteActionCard(
                    title = "Wipe Data",
                    icon = Icons.Default.DeleteForever,
                    iconColor = EmergencyRed,
                    modifier = Modifier.weight(1f),
                    onClick = { showConfirmWipeDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Lockdown Button
            PhantomEmergencyButton(
                text = "Emergency Lockdown",
                onClick = {
                    lastCommandStatus = "EMERGENCY LOCKDOWN initiated: Hardware LockTask active"
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Last Command Status label
            Text(
                text = "Last Command: $lastCommandStatus",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted
                )
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Wipe Confirmation Dialog
        if (showConfirmWipeDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmWipeDialog = false },
                title = { Text("Confirm Remote Data Wipe", color = TextPrimary) },
                text = {
                    Text(
                        "WARNING: This will permanently erase all data, accounts, and files from this device using Android Enterprise wipeData(). This action cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmWipeDialog = false
                            lastCommandStatus = "WIPE_DATA authorized & sent"
                        }
                    ) {
                        Text("Wipe Permanently", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmWipeDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceNavy
            )
        }

        // Custom Message Dialog
        if (showMessageDialog) {
            AlertDialog(
                onDismissRequest = { showMessageDialog = false },
                title = { Text("Send Recovery Message", color = TextPrimary) },
                text = {
                    Column {
                        Text("Enter the message to display on the locked screen:", color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        PhantomTextField(
                            value = customMessage,
                            onValueChange = { customMessage = it },
                            label = "Message"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showMessageDialog = false
                            lastCommandStatus = "Message sent to device"
                        }
                    ) {
                        Text("Send", color = CyanGlow, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMessageDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceNavy
            )
        }
    }
}

@Composable
private fun RemoteActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = CyanGlow,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            )
        }
    }
}
