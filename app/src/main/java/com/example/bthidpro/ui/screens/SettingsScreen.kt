package com.example.bthidpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: HidViewModel, onBack: () -> Unit) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val scrollSpeed by viewModel.scrollSpeed.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
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
            Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Mouse Settings ────────────────────────────────────────────────
            SettingsSection(title = "Mouse", icon = Icons.Default.Mouse) {
                SettingsSlider(
                    label = "Pointer Sensitivity",
                    value = sensitivity,
                    valueRange = 1f..10f,
                    displayValue = "${sensitivity.roundToInt()}×",
                    onValueChange = { viewModel.setSensitivity(it) }
                )
                HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingsSlider(
                    label = "Scroll Speed",
                    value = scrollSpeed,
                    valueRange = 1f..10f,
                    displayValue = "${scrollSpeed.roundToInt()}×",
                    onValueChange = { viewModel.setScrollSpeed(it) }
                )
            }

            // ── Haptics ───────────────────────────────────────────────────────
            SettingsSection(title = "Haptics", icon = Icons.Default.Vibration) {
                SettingsToggle(
                    label = "Button Vibration",
                    description = "Vibrate on key & button presses",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }

            // ── Device Info ───────────────────────────────────────────────────
            SettingsSection(title = "HID Device", icon = Icons.Default.Bluetooth) {
                SettingsInfoRow(label = "Device Name", value = "BT HID Pro")
                HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoRow(label = "Report Protocol", value = "Keyboard + Mouse + Gamepad")
                HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoRow(label = "Min Android Version", value = "API 28 (Android 9.0)")
            }

            // ── About ──────────────────────────────────────────────────────────
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                SettingsInfoRow(label = "App Version", value = "1.0.0")
                HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoRow(label = "License", value = "MIT Open Source")
                HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoRow(label = "HID Spec", value = "USB HID 1.11")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Settings Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                title,
                color = Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                displayValue,
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Primary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = ((valueRange.endInclusive - valueRange.start).roundToInt() - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = DarkSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(description, color = OnDarkSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = OnDarkTertiary,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OnDarkSecondary, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
