package com.example.bthidpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bthidpro.hid.GamepadButton
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel
import kotlin.math.roundToInt

@Composable
fun GamepadScreen(viewModel: HidViewModel, onBack: () -> Unit) {

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
            Text("Gamepad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Success)
            )
            Spacer(Modifier.width(12.dp))
        }

        // ── Shoulder buttons (L2 / L1 / R1 / R2) ────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShoulderButton("L2", GamepadButton.L2, viewModel, Modifier.weight(1f))
            ShoulderButton("L1", GamepadButton.L1, viewModel, Modifier.weight(1f))
            Spacer(Modifier.weight(2f))
            ShoulderButton("R1", GamepadButton.R1, viewModel, Modifier.weight(1f))
            ShoulderButton("R2", GamepadButton.R2, viewModel, Modifier.weight(1f))
        }

        // ── Main gamepad body ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left side: D-Pad + Left Stick ─────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                DPad(viewModel = viewModel)
                AnalogStick(
                    label = "L",
                    size = 110.dp,
                    onMove = { x, y -> viewModel.updateLeftStick(x, y) }
                )
            }

            // ── Centre: Select / Menu / Start ──────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                CentreButton("SEL", GamepadButton.SELECT, viewModel)
                CentreButton("☰", GamepadButton.MENU, viewModel)
                CentreButton("STA", GamepadButton.START, viewModel)
            }

            // ── Right side: Face buttons + Right Stick ────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                FaceButtons(viewModel = viewModel)
                AnalogStick(
                    label = "R",
                    size = 110.dp,
                    onMove = { x, y -> viewModel.updateRightStick(x, y) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// D-Pad
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DPad(viewModel: HidViewModel) {
    val size = 110.dp
    val armW = size / 3f
    val armH = size / 3f

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        DPadArm("▲", GamepadButton.DPAD_UP, viewModel,
            Modifier.width(armW).height(armH).align(Alignment.TopCenter))
        DPadArm("▼", GamepadButton.DPAD_DOWN, viewModel,
            Modifier.width(armW).height(armH).align(Alignment.BottomCenter))
        DPadArm("◀", GamepadButton.DPAD_LEFT, viewModel,
            Modifier.height(armW).width(armH).align(Alignment.CenterStart))
        DPadArm("▶", GamepadButton.DPAD_RIGHT, viewModel,
            Modifier.height(armW).width(armH).align(Alignment.CenterEnd))
        Box(
            modifier = Modifier
                .size(armW)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkSurfaceVariant)
        )
    }
}

@Composable
private fun DPadArm(
    label: String,
    bit: Int,
    viewModel: HidViewModel,
    modifier: Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (pressed) Primary else DarkSurfaceVariant)
            .border(1.dp, OnDarkTertiary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        viewModel.pressGamepadButton(bit)
                        tryAwaitRelease()
                        pressed = false
                        viewModel.releaseGamepadButton(bit)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (pressed) Color.White else OnDarkSecondary, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Analog Stick
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnalogStick(
    label: String,
    size: Dp,
    onMove: (Int, Int) -> Unit
) {
    var stickX by remember { mutableFloatStateOf(0f) }
    var stickY by remember { mutableFloatStateOf(0f) }
    val radius = size / 2f

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(DarkSurface)
            .border(2.dp, OnDarkTertiary.copy(alpha = 0.3f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        stickX = (stickX + dragAmount.x)
                            .coerceIn(-radius.toPx() * 0.6f, radius.toPx() * 0.6f)
                        stickY = (stickY + dragAmount.y)
                            .coerceIn(-radius.toPx() * 0.6f, radius.toPx() * 0.6f)
                        val nx = ((stickX / (radius.toPx() * 0.6f)) * 127).roundToInt()
                        val ny = ((stickY / (radius.toPx() * 0.6f)) * 127).roundToInt()
                        onMove(nx, ny)
                    },
                    onDragEnd = {
                        stickX = 0f; stickY = 0f; onMove(0, 0)
                    },
                    onDragCancel = {
                        stickX = 0f; stickY = 0f; onMove(0, 0)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = stickX.dp / 3f, y = stickY.dp / 3f)
                .size(size * 0.5f)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Primary, PrimaryDark))),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Face Buttons — CROSS(A) / CIRCLE(B) / SQUARE(X) / TRIANGLE(Y)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FaceButtons(viewModel: HidViewModel) {
    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
        // Y / Triangle — top
        FaceButton("Y", ButtonY, GamepadButton.TRIANGLE, viewModel,
            Modifier.align(Alignment.TopCenter))
        // A / Cross — bottom
        FaceButton("A", ButtonA, GamepadButton.CROSS, viewModel,
            Modifier.align(Alignment.BottomCenter))
        // X / Square — left
        FaceButton("X", ButtonX, GamepadButton.SQUARE, viewModel,
            Modifier.align(Alignment.CenterStart))
        // B / Circle — right
        FaceButton("B", ButtonB, GamepadButton.CIRCLE, viewModel,
            Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun FaceButton(
    label: String,
    color: Color,
    bit: Int,
    viewModel: HidViewModel,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (pressed) color else color.copy(alpha = 0.25f))
            .border(2.dp, color, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        viewModel.pressGamepadButton(bit)
                        tryAwaitRelease()
                        pressed = false
                        viewModel.releaseGamepadButton(bit)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (pressed) Color.White else color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shoulder Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShoulderButton(
    label: String,
    bit: Int,
    viewModel: HidViewModel,
    modifier: Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (pressed) Primary else DarkSurface)
            .border(
                1.dp,
                if (pressed) Primary else OnDarkTertiary.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        viewModel.pressGamepadButton(bit)
                        tryAwaitRelease()
                        pressed = false
                        viewModel.releaseGamepadButton(bit)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (pressed) Color.White else OnDarkSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Centre Button (Select / Menu / Start)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CentreButton(label: String, bit: Int, viewModel: HidViewModel) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (pressed) Accent else DarkSurfaceVariant)
            .border(
                1.dp,
                if (pressed) Accent else OnDarkTertiary.copy(alpha = 0.3f),
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        viewModel.pressGamepadButton(bit)
                        tryAwaitRelease()
                        pressed = false
                        viewModel.releaseGamepadButton(bit)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (pressed) Color.White else OnDarkSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
