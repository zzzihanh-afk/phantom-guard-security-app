package com.phantomguard.security.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomguard.security.data.model.UserRole
import com.phantomguard.security.data.repository.SecurityRepository
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToProtectionSettings: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToGlobalAdmin: () -> Unit,
    onNavigateToDatabaseConfig: () -> Unit,
    onLogout: () -> Unit
) {
    val repository = SecurityRepository.instance
    val currentUser by repository.currentUser.collectAsState()

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(NeonBlue.copy(alpha = 0.2f))
                            .border(1.5.dp, CyanGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = currentUser?.displayName ?: "User",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = currentUser?.email ?: "user@phantomguard.security",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentUser?.role == UserRole.GLOBAL_MASTER_ADMIN) "👑 Global Master Admin" else "🛡️ Registered Admin",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (currentUser?.role == UserRole.GLOBAL_MASTER_ADMIN) Color(0xFFEAB308) else CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Security & Protection",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Database & Cloud Sync",
                subtitle = "Manage Cloud Firestore & offline SQLite store",
                icon = Icons.Default.CloudDone,
                iconColor = SecurityGreen,
                onClick = onNavigateToDatabaseConfig
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Permissions & System Access Hub",
                subtitle = "Grant Location, Phone State, Device Admin, and Battery access",
                icon = Icons.Default.Key,
                iconColor = CyanGlow,
                onClick = onNavigateToCompatibility
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Protection Settings",
                subtitle = "Manage anti-theft, lockdown, and uninstall protection",
                icon = Icons.Default.Shield,
                iconColor = SecurityGreen,
                onClick = onNavigateToProtectionSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Universal Compatibility Report",
                subtitle = "Audit hardware, root, bootloader, and OEM status",
                icon = Icons.Default.Analytics,
                iconColor = CyanGlow,
                onClick = onNavigateToCompatibility
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Administration & Roles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Global Master Admin Panel",
                subtitle = "Access platform-wide analytics, device & user controls",
                icon = Icons.Default.AdminPanelSettings,
                iconColor = Color(0xFFEAB308),
                onClick = onNavigateToGlobalAdmin
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Role Switcher Tile (for pair-programming test/demo convenience)
            SettingsTile(
                title = "Switch Active Role",
                subtitle = "Current: ${currentUser?.role?.name}",
                icon = Icons.Default.SwapHoriz,
                iconColor = NeonBlue,
                onClick = {
                    val nextRole = if (currentUser?.role == UserRole.GLOBAL_MASTER_ADMIN) {
                        UserRole.REGISTERED_ADMIN
                    } else {
                        UserRole.GLOBAL_MASTER_ADMIN
                    }
                    repository.switchRole(nextRole)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsTile(
                title = "Logout",
                subtitle = "Sign out of this session",
                icon = Icons.Default.Logout,
                iconColor = EmergencyRed,
                onClick = {
                    repository.logout()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // App Version Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PHANTOM GUARD",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = TextSecondary
                    )
                )
                Text(
                    text = "Version 1.0.0 (Production Release)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                Text(
                    text = "LOCK • TRACK • PROTECT",
                    style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontSize = 10.sp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
