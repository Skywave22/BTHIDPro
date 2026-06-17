package com.example.bthidpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel
import kotlin.math.roundToInt

@Composable
fun MouseScreen(viewModel: HidViewModel, onBack: () -> Unit) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val scrollSpeed by viewModel.scrollSpeed.collectAsState()

    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }
    var middlePressed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar(title = "Mouse", onBack = onBack)

        Spacer(Modifier.height(8.dp))

        // ── Info chip ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoChip(Icons.Default.TouchApp, "Drag to move")
            InfoChip(Icons.Default.Swipe, "Two-finger drag: scroll")
            InfoChip(Icons.Default.Mouse, "Tap: left click")
        }

        Spacer(Modifier.height(12.dp))

        // ── Touchpad ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DarkSurface, DarkSurfaceVariant)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Primary.copy(alpha = 0.4f), Accent.copy(alpha = 0.2f))),
                    shape = RoundedCornerShape(24.dp)
                )
                .pointerInput(sensitivity) {
                    detectDragGestures { _, dragAmount ->
                        val dx = (dragAmount.x * sensitivity / 5f).roundToInt()
                        val dy = (dragAmount.y * sensitivity / 5f).roundToInt()
                        viewModel.sendMouseMove(dx, dy)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            viewModel.sendMouseButton(left = true, right = false, middle = false)
                            tryAwaitRelease()
                            viewModel.sendMouseButton(left = false, right = false, middle = false)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Mouse,
                    contentDescription = null,
                    tint = Primary.copy(alpha = 0.25f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Touchpad",
                    color = OnDarkTertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Drag to move • Tap to left-click",
                    color = OnDarkTertiary.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Scroll strip ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = OnDarkSecondary, modifier = Modifier.size(20.dp))
            Text("Scroll", color = OnDarkSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            MouseActionButton("▲") {
                viewModel.sendMouseScroll(-(scrollSpeed.roundToInt().coerceAtLeast(1)))
            }
            MouseActionButton("▼") {
                viewModel.sendMouseScroll(scrollSpeed.roundToInt().coerceAtLeast(1))
            }
            Spacer(Modifier.width(8.dp))
            MouseActionButton("◀") {
                // Horizontal scroll left (some hosts support it)
                viewModel.sendMouseScroll(0)
            }
            MouseActionButton("▶") {
                viewModel.sendMouseScroll(0)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Mouse buttons ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MouseClickButton(
                label = "Left Click",
                icon = Icons.Default.TouchApp,
                color = Primary,
                pressed = leftPressed,
                modifier = Modifier.weight(1f),
                onPress = {
                    leftPressed = true
                    viewModel.sendMouseButton(left = true, right = false, middle = false)
                },
                onRelease = {
                    leftPressed = false
                    viewModel.sendMouseButton(left = false, right = false, middle = false)
                }
            )
            MouseClickButton(
                label = "Middle",
                icon = Icons.Default.RadioButtonUnchecked,
                color = Accent,
                pressed = middlePressed,
                modifier = Modifier.weight(0.6f),
                onPress = {
                    middlePressed = true
                    viewModel.sendMouseButton(left = false, right = false, middle = true)
                },
                onRelease = {
                    middlePressed = false
                    viewModel.sendMouseButton(left = false, right = false, middle = false)
                }
            )
            MouseClickButton(
                label = "Right Click",
                icon = Icons.Default.TouchApp,
                color = Error,
                pressed = rightPressed,
                modifier = Modifier.weight(1f),
                onPress = {
                    rightPressed = true
                    viewModel.sendMouseButton(left = false, right = true, middle = false)
                },
                onRelease = {
                    rightPressed = false
                    viewModel.sendMouseButton(left = false, right = false, middle = false)
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
        Text(label, color = OnDarkSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun MouseActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = OnDarkSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun MouseClickButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    pressed: Boolean,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (pressed) color.copy(alpha = 0.25f)
                else DarkSurface
            )
            .border(
                width = if (pressed) 2.dp else 1.dp,
                color = if (pressed) color else OnDarkTertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (pressed) color else OnDarkSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = if (pressed) color else OnDarkSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
