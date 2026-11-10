package com.phantomguard.security.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomguard.security.data.repository.SecurityRepository
import com.phantomguard.security.ui.components.PhantomButton
import com.phantomguard.security.ui.components.PhantomTextField
import com.phantomguard.security.ui.components.PhantomTopBar
import com.phantomguard.security.ui.theme.*

@Composable
fun AddDeviceScreen(
    onBackClick: () -> Unit,
    onDeviceEnrolled: () -> Unit
) {
    var deviceName by remember { mutableStateOf("Google Pixel 9 Pro") }
    var manufacturer by remember { mutableStateOf("Google") }
    var model by remember { mutableStateOf("GE2AE") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            PhantomTopBar(
                title = "Enroll New Device",
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
            Text(
                text = "Device Identification",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            PhantomTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = "Device Display Name"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PhantomTextField(
                value = manufacturer,
                onValueChange = { manufacturer = it },
                label = "Manufacturer (e.g. Xiaomi, Samsung, Google)"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PhantomTextField(
                value = model,
                onValueChange = { model = it },
                label = "Model Identifier"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Zero-Touch QR Enrollment Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, CyanGlow.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android Enterprise Zero-Touch QR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 6 times on any blank area of a factory reset phone to scan enrollment configuration payload.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            PhantomButton(
                text = "Enroll Device Now",
                isLoading = isSubmitting,
                icon = Icons.Default.Add,
                onClick = {
                    isSubmitting = true
                    SecurityRepository.instance.addDevice(deviceName, manufacturer, model)
                    onDeviceEnrolled()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
