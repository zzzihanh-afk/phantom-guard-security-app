package com.phantomguard.security.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomguard.security.data.model.AlertSeverity
import com.phantomguard.security.data.model.SecurityAlert
import com.phantomguard.security.data.repository.SecurityRepository
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*

@Composable
fun SecurityAlertsScreen(
    onBackClick: () -> Unit
) {
    val repository = SecurityRepository.instance
    val alerts by repository.alerts.collectAsState()

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Security Alerts (${alerts.size})",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(alerts) { alert ->
                SecurityAlertItem(alert = alert)
            }
        }
    }
}

@Composable
private fun SecurityAlertItem(
    alert: SecurityAlert
) {
    val (bgColor, borderColor, iconTint) = when (alert.severity) {
        AlertSeverity.CRITICAL -> Triple(Color(0xFF381216), Color(0xFFEF4444).copy(alpha = 0.5f), EmergencyRed)
        AlertSeverity.HIGH -> Triple(Color(0xFF38260F), Color(0xFFF59E0B).copy(alpha = 0.5f), AlertAmber)
        AlertSeverity.MEDIUM -> Triple(Color(0xFF2E240D), Color(0xFFEAB308).copy(alpha = 0.5f), Color(0xFFEAB308))
        AlertSeverity.INFO -> Triple(Color(0xFF0D2E20), Color(0xFF10B981).copy(alpha = 0.5f), SecurityGreen)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.severity == AlertSeverity.INFO) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = alert.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
