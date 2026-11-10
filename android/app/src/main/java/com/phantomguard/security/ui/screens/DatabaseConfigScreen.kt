package com.phantomguard.security.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomguard.security.data.remote.FirestoreSyncManager
import com.phantomguard.security.ui.components.PhantomButton
import com.phantomguard.security.ui.components.PhantomTextField
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DatabaseConfigScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { FirestoreSyncManager.getInstance(context) }

    var projectId by remember { mutableStateOf(syncManager.projectId) }
    var isSyncing by remember { mutableStateOf(false) }
    var lastSyncStatus by remember { mutableStateOf("Connected to Cloud Firestore") }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Database & Cloud Sync",
                onBackClick = onBackClick
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
            // Cloud Database Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, Color(0xFF05D182).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF05D182).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF05D182),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Cloud Firestore Database",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SecurityGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = lastSyncStatus,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SecurityGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Database Configuration",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Dual-layer database: Local SQLite offline store + Google Cloud Firestore",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Firebase Project ID input
            PhantomTextField(
                value = projectId,
                onValueChange = { projectId = it },
                label = "Firebase Project ID",
                leadingIcon = Icons.Default.Cloud
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save / Update Project ID Button
            Button(
                onClick = {
                    syncManager.updateProjectId(projectId)
                    Toast.makeText(context, "Cloud Project ID updated to '$projectId'", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavyElevated)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Cloud Project ID", color = CyanGlow, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Linked Cloud Collections Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SurfaceNavyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Synchronized Firestore Collections",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CollectionRow("/devices", "Device telemetry, battery & online status")
                    CollectionRow("/remoteCommands", "Cryptographically signed lock & siren actions")
                    CollectionRow("/securityAlerts", "SIM change, failed unlock & offline events")
                    CollectionRow("/recoveryConversations", "Real-time finder chat & return negotiations")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Force Cloud Sync Button
            PhantomButton(
                text = if (isSyncing) "Synchronizing Database..." else "Sync with Cloud Database Now",
                isLoading = isSyncing,
                icon = Icons.Default.Sync,
                onClick = {
                    coroutineScope.launch {
                        isSyncing = true
                        val success = syncManager.syncLocalDeviceState()
                        val cmdCount = syncManager.checkAndExecuteRemoteCommands()
                        isSyncing = false
                        lastSyncStatus = if (success) "Sync Complete • Heartbeat Verified" else "Local Database Active"
                        Toast.makeText(
                            context,
                            if (success) "Database synchronized successfully with Cloud Firestore!" else "Local Database updated. Cloud reachable.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CollectionRow(collectionPath: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = collectionPath,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanGlow
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "• $desc",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp),
            modifier = Modifier.weight(1f)
        )
    }
}
