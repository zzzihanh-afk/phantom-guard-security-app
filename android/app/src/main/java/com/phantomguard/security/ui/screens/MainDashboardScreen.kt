package com.phantomguard.security.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phantomguard.security.data.model.UserRole
import com.phantomguard.security.data.repository.SecurityRepository
import com.phantomguard.security.domain.engine.DeviceCompatibilityEngine
import com.phantomguard.security.ui.components.*
import com.phantomguard.security.ui.theme.*

@Composable
fun MainDashboardScreen(
    onNavigateToDevices: () -> Unit,
    onNavigateToRemoteControl: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGlobalAdmin: () -> Unit,
    onNavigateToCompatibility: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = SecurityRepository.instance
    val currentUser by repository.currentUser.collectAsState()
    val devices by repository.devices.collectAsState()
    val alerts by repository.alerts.collectAsState()

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

    val localReport = remember(refreshCounter) { engine.evaluateDevice() }

    val onlineCount = devices.count { it.isOnline }
    val featuredDevice = devices.firstOrNull()

    Scaffold(
        containerColor = DarkNavyBg,
        bottomBar = {
            PhantomBottomNav(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "devices" -> onNavigateToDevices()
                        "alerts" -> onNavigateToAlerts()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Hello, ${currentUser?.displayName ?: "User"}!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Your Security, Our Priority",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToAlerts) {
                        BadgedBox(
                            badge = {
                                if (alerts.isNotEmpty()) {
                                    Badge(containerColor = EmergencyRed) {
                                        Text(text = alerts.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = TextPrimary
                            )
                        }
                    }

                    // Global Master Admin Switcher Pill
                    IconButton(onClick = onNavigateToGlobalAdmin) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceNavyElevated)
                                .border(1.dp, CyanGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Global Admin",
                                tint = CyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attention Banner if Device Admin or Permissions are inactive on this physical device
            if (!localReport.isDeviceAdmin || !localReport.hasLocationPermission || !localReport.hasPhoneStatePermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceNavyElevated)
                        .border(1.5.dp, AlertAmber, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCompatibility() }
                        .padding(14.dp)
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
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (!localReport.isDeviceAdmin) "Device Administrator Inactive" else "Permissions Required",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Tap here to Grant Permissions & Activate Lock",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanGlow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Grant →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Security Status Banner Card
            val isFullyProtected = localReport.isDeviceAdmin && localReport.hasLocationPermission
            val bannerGradient = if (isFullyProtected) {
                listOf(Color(0xFF0D3325), SurfaceNavy)
            } else {
                listOf(Color(0xFF38260F), SurfaceNavy)
            }
            val bannerBorderColor = if (isFullyProtected) Color(0xFF10B981) else AlertAmber

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(colors = bannerGradient))
                    .border(BorderStroke(1.dp, bannerBorderColor.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToCompatibility() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(bannerBorderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyProtected) Icons.Default.CheckCircle else Icons.Default.Shield,
                            contentDescription = null,
                            tint = bannerBorderColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isFullyProtected) "All Devices Secure" else "Protection Setup Needed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Text(
                        text = if (isFullyProtected) "Excellent Protection" else "Tap to Grant Required Permissions",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isFullyProtected) Color(0xFF05D182) else CyanGlow,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4-Grid Metric Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PhantomStatCard(
                    count = "${devices.size}",
                    label = "Devices",
                    icon = Icons.Default.PhoneAndroid,
                    iconColor = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
                PhantomStatCard(
                    count = if (isFullyProtected) "100%" else "Limited",
                    label = "Protected",
                    icon = Icons.Default.VerifiedUser,
                    iconColor = if (isFullyProtected) SecurityGreen else AlertAmber,
                    modifier = Modifier.weight(1f)
                )
                PhantomStatCard(
                    count = "$onlineCount",
                    label = "Online",
                    icon = Icons.Default.LocationOn,
                    iconColor = CyanGlow,
                    modifier = Modifier.weight(1f)
                )
                PhantomStatCard(
                    count = "${alerts.size}",
                    label = "Alerts",
                    icon = Icons.Default.Warning,
                    iconColor = AlertAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Your Devices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Devices",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CyanGlow,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { onNavigateToDevices() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Featured Device Card
            featuredDevice?.let { device ->
                PhantomCard(
                    onClick = { onNavigateToRemoteControl(device.id) },
                    borderColor = CyanGlow.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceNavyElevated)
                                    .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
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
                                            .background(if (device.isOnline) SecurityGreen else AlertAmber)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (device.isOnline) "Online" else "Offline",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (device.isOnline) SecurityGreen else AlertAmber
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${device.batteryLevel}%",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }

                        // Deep Protection Pill Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SecurityGreen.copy(alpha = 0.15f))
                                .border(1.dp, SecurityGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Deep Protection",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SecurityGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
