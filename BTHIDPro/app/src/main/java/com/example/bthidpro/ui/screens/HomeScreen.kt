package com.example.bthidpro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bthidpro.*
import com.example.bthidpro.hid.ConnectionState
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel

@Composable
fun HomeScreen(
    viewModel: HidViewModel,
    onNavigate: (Any) -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkSurface, DarkBackground)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Primary, Accent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("BT HID Pro", fontWeight = FontWeight.Bold,
                            fontSize = 22.sp, color = Color.White)
                        Text("Keyboard · Mouse · Gamepad",
                            fontSize = 13.sp, color = OnDarkSecondary)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onNavigate(SettingsRoute) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings",
                            tint = OnDarkSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Connection Status Card ──────────────────────────────────────────────
        ConnectionCard(
            state = connectionState,
            onConnect = { onNavigate(DevicePickerRoute) },
            onDisconnect = { viewModel.disconnect() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ── Section Title ─────────────────────────────────────────────────────
        Text(
            "Control Modes",
            modifier = Modifier.padding(horizontal = 24.dp),
            color = OnDarkSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(12.dp))

        // ── Mode Cards ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                icon = Icons.Default.Keyboard,
                title = "Keyboard",
                description = "Full QWERTY with function keys, modifiers & shortcuts",
                gradient = listOf(Color(0xFF6C63FF), Color(0xFF4B44CC)),
                enabled = connectionState == ConnectionState.CONNECTED,
                onClick = { onNavigate(KeyboardRoute) }
            )
            ModeCard(
                icon = Icons.Default.Mouse,
                title = "Mouse",
                description = "Touchpad with left/right click, scroll & precision control",
                gradient = listOf(Color(0xFF00D4AA), Color(0xFF009C7A)),
                enabled = connectionState == ConnectionState.CONNECTED,
                onClick = { onNavigate(MouseRoute) }
            )
            ModeCard(
                icon = Icons.Default.SportsEsports,
                title = "Gamepad",
                description = "Dual sticks, D-Pad, 16 buttons — works with any game",
                gradient = listOf(Color(0xFFFF6B9D), Color(0xFFCC4477)),
                enabled = connectionState == ConnectionState.CONNECTED,
                onClick = { onNavigate(GamepadRoute) }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Quick Tips ────────────────────────────────────────────────────────
        Text(
            "Quick Tips",
            modifier = Modifier.padding(horizontal = 24.dp),
            color = OnDarkSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TipRow(icon = Icons.Default.BluetoothSearching, text = "Pair this device from your PC's Bluetooth settings first")
            TipRow(icon = Icons.Default.TouchApp, text = "Mouse: drag to move, tap to click, two-finger drag to scroll")
            TipRow(icon = Icons.Default.SportsEsports, text = "Gamepad: recognized as standard controller by Steam & Windows")
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ConnectionCard(
    state: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor, statusIcon) = when (state) {
        ConnectionState.CONNECTED -> Triple("Connected", Success, Icons.Default.CheckCircle)
        ConnectionState.CONNECTING -> Triple("Connecting...", Warning, Icons.Default.Sync)
        ConnectionState.REGISTERED -> Triple("Ready to Connect", Info, Icons.Default.BluetoothSearching)
        ConnectionState.DISCONNECTING -> Triple("Disconnecting...", Warning, Icons.Default.Sync)
        ConnectionState.ERROR -> Triple("Bluetooth Error", Error, Icons.Default.Error)
        ConnectionState.DISCONNECTED -> Triple("Service Starting...", OnDarkTertiary, Icons.Default.BluetoothDisabled)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(statusIcon, contentDescription = null,
                        tint = statusColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Status", color = OnDarkTertiary, fontSize = 12.sp)
                    Text(statusText, color = statusColor,
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state != ConnectionState.CONNECTED) {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = state == ConnectionState.REGISTERED || state == ConnectionState.DISCONNECTED
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Connect", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                    ) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Disconnect", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: List<Color>,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100), label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (enabled) Brush.linearGradient(gradient) else Brush.linearGradient(listOf(DarkSurfaceVariant, DarkSurfaceVariant))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null,
                    tint = if (enabled) Color.White else OnDarkTertiary,
                    modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (enabled) Color.White else OnDarkTertiary,
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(3.dp))
                Text(description, color = OnDarkSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (enabled) OnDarkSecondary else OnDarkTertiary
            )
        }
    }
}

@Composable
private fun TipRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = Primary,
            modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = OnDarkSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
