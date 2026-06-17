package com.example.bthidpro.ui.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bthidpro.hid.ConnectionState
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel

@Composable
fun DevicePickerScreen(viewModel: HidViewModel, onBack: () -> Unit) {
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val context = LocalContext.current

    // When we become CONNECTED, navigate back automatically
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            onBack()
        }
    }

    // Refresh device list on open
    LaunchedEffect(Unit) {
        viewModel.refreshDevices()
    }

    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Top bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Select Device", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshDevices() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnDarkSecondary)
            }
        }

        // ── Connecting banner ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = connectionState == ConnectionState.CONNECTING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ConnectingBanner()
        }

        Spacer(Modifier.height(16.dp))

        // ── Bluetooth pulse animation header ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            PulsingBluetoothIcon()
        }

        Spacer(Modifier.height(16.dp))

        // ── Description ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Paired Bluetooth Devices",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Select a device to connect. Make sure you've paired this phone from the host's Bluetooth settings first.",
                color = OnDarkSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        if (!hasPermission) {
            // No permission message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Error.copy(alpha = 0.12f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "BLUETOOTH_CONNECT permission is required to list devices.",
                        color = Error,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        } else if (pairedDevices.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        tint = OnDarkTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No paired devices found", color = OnDarkSecondary, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pair your PC/Mac from its Bluetooth settings,\nthen tap Refresh.",
                        color = OnDarkTertiary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.refreshDevices() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Refresh", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Device list
            Text(
                "${pairedDevices.size} device${if (pairedDevices.size != 1) "s" else ""} found",
                color = OnDarkTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pairedDevices) { device ->
                    DeviceCard(
                        device = device,
                        isConnecting = connectionState == ConnectionState.CONNECTING,
                        onClick = {
                            if (connectionState != ConnectionState.CONNECTING) {
                                viewModel.connectToDevice(device)
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pulsing Bluetooth Icon
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PulsingBluetoothIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Primary.copy(alpha = alpha))
        )
        // Inner circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Primary, PrimaryDark))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.BluetoothSearching,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Connecting Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConnectingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary.copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = Primary,
            strokeWidth = 2.dp
        )
        Text("Connecting...", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Device Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeviceCard(
    device: BluetoothDevice,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    val deviceName = try {
        device.name ?: "Unknown Device"
    } catch (e: SecurityException) {
        "Unknown Device"
    }
    val deviceAddress = device.address ?: ""

    val deviceIcon = when {
        deviceName.contains("keyboard", ignoreCase = true) -> Icons.Default.Keyboard
        deviceName.contains("mouse", ignoreCase = true) -> Icons.Default.Mouse
        deviceName.contains("phone", ignoreCase = true) -> Icons.Default.Smartphone
        deviceName.contains("laptop", ignoreCase = true) ||
            deviceName.contains("mac", ignoreCase = true) ||
            deviceName.contains("windows", ignoreCase = true) -> Icons.Default.Laptop
        else -> Icons.Default.DevicesOther
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isConnecting, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon in gradient circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, Accent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    deviceIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    deviceName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    deviceAddress,
                    color = OnDarkTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Connect",
                    tint = OnDarkSecondary
                )
            }
        }
    }
}
