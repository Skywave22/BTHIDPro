package com.example.bthidpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bthidpro.hid.KeyCode
import com.example.bthidpro.theme.*
import com.example.bthidpro.ui.HidViewModel

data class KeyDef(
    val label: String,
    val modifier: Byte = KeyCode.MOD_NONE,
    val keyCode: Byte = KeyCode.NONE,
    val shiftLabel: String = "",
    val widthWeight: Float = 1f,
    val isSpecial: Boolean = false
)

@Composable
fun KeyboardScreen(viewModel: HidViewModel, onBack: () -> Unit) {
    var isShift by remember { mutableStateOf(false) }
    var isCaps by remember { mutableStateOf(false) }
    var isCtrl by remember { mutableStateOf(false) }
    var isAlt by remember { mutableStateOf(false) }
    var typeText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val effectiveShift = isShift || isCaps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        TopBar(title = "Keyboard", onBack = onBack)

        // Text preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(12.dp)
        ) {
            Text(
                text = if (typeText.isEmpty()) "Typed text appears here..." else typeText,
                color = if (typeText.isEmpty()) OnDarkTertiary else Color.White,
                fontSize = 15.sp
            )
        }

        // Modifier row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModifierChip("CTRL", isCtrl) { isCtrl = !isCtrl }
            ModifierChip("ALT", isAlt) { isAlt = !isAlt }
            ModifierChip("SHIFT", isShift) { isShift = !isShift }
            ModifierChip("CAPS", isCaps) { isCaps = !isCaps }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { typeText = ""; viewModel.sendKeyRelease() }) {
                Text("Clear", color = Error, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Keyboard rows
        val keyRows = buildKeyRows()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Function row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("F1" to KeyCode.F1, "F2" to KeyCode.F2, "F3" to KeyCode.F3,
                    "F4" to KeyCode.F4, "F5" to KeyCode.F5, "F6" to KeyCode.F6,
                    "F7" to KeyCode.F7, "F8" to KeyCode.F8, "F9" to KeyCode.F9,
                    "F10" to KeyCode.F10, "F11" to KeyCode.F11, "F12" to KeyCode.F12
                ).forEach { (label, code) ->
                    KeyButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        isSpecial = true,
                        onPress = {
                            viewModel.sendKeyPress(KeyCode.MOD_NONE, code)
                        },
                        onRelease = { viewModel.sendKeyRelease() }
                    )
                }
            }

            // Main rows
            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (key in row) {
                        val displayLabel = if (effectiveShift && key.shiftLabel.isNotEmpty())
                            key.shiftLabel else key.label
                        KeyButton(
                            label = displayLabel,
                            modifier = Modifier.weight(key.widthWeight),
                            isSpecial = key.isSpecial,
                            isActive = when (key.label) {
                                "SHIFT" -> isShift
                                "CAPS" -> isCaps
                                "CTRL" -> isCtrl
                                "ALT" -> isAlt
                                else -> false
                            },
                            onPress = {
                                val mod = buildModifier(isCtrl, effectiveShift, isAlt)
                                when (key.label) {
                                    "SHIFT" -> isShift = !isShift
                                    "CAPS" -> { isCaps = !isCaps }
                                    "CTRL" -> isCtrl = !isCtrl
                                    "ALT" -> isAlt = !isAlt
                                    "⌫" -> {
                                        typeText = typeText.dropLast(1)
                                        viewModel.sendKeyPress(mod, KeyCode.BACKSPACE)
                                    }
                                    "ENTER" -> {
                                        typeText += "\n"
                                        viewModel.sendKeyPress(mod, KeyCode.ENTER)
                                    }
                                    "TAB" -> {
                                        typeText += "\t"
                                        viewModel.sendKeyPress(mod, KeyCode.TAB)
                                    }
                                    "SPACE" -> {
                                        typeText += " "
                                        viewModel.sendKeyPress(mod, KeyCode.SPACE)
                                    }
                                    "ESC" -> viewModel.sendKeyPress(mod, KeyCode.ESCAPE)
                                    "DEL" -> viewModel.sendKeyPress(mod, KeyCode.DELETE)
                                    "↑" -> viewModel.sendKeyPress(mod, KeyCode.UP_ARROW)
                                    "↓" -> viewModel.sendKeyPress(mod, KeyCode.DOWN_ARROW)
                                    "←" -> viewModel.sendKeyPress(mod, KeyCode.LEFT_ARROW)
                                    "→" -> viewModel.sendKeyPress(mod, KeyCode.RIGHT_ARROW)
                                    else -> {
                                        if (key.keyCode != KeyCode.NONE) {
                                            typeText += displayLabel
                                            viewModel.sendKeyPress(mod, key.keyCode)
                                        }
                                    }
                                }
                                if (isShift && key.label != "SHIFT") isShift = false
                            },
                            onRelease = { viewModel.sendKeyRelease() }
                        )
                    }
                }
            }

            // Arrow row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(Modifier.weight(1f))
                KeyButton("ESC", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.ESCAPE) },
                    onRelease = { viewModel.sendKeyRelease() })
                KeyButton("DEL", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.DELETE) },
                    onRelease = { viewModel.sendKeyRelease() })
                KeyButton("↑", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.UP_ARROW) },
                    onRelease = { viewModel.sendKeyRelease() })
                Spacer(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
                KeyButton("←", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.LEFT_ARROW) },
                    onRelease = { viewModel.sendKeyRelease() })
                KeyButton("↓", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.DOWN_ARROW) },
                    onRelease = { viewModel.sendKeyRelease() })
                KeyButton("→", Modifier.weight(1f), isSpecial = true,
                    onPress = { viewModel.sendKeyPress(KeyCode.MOD_NONE, KeyCode.RIGHT_ARROW) },
                    onRelease = { viewModel.sendKeyRelease() })
                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun buildModifier(ctrl: Boolean, shift: Boolean, alt: Boolean): Byte {
    var mod = 0
    if (ctrl) mod = mod or KeyCode.MOD_LEFT_CTRL.toInt()
    if (shift) mod = mod or KeyCode.MOD_LEFT_SHIFT.toInt()
    if (alt) mod = mod or KeyCode.MOD_LEFT_ALT.toInt()
    return mod.toByte()
}

private fun buildKeyRows(): List<List<KeyDef>> = listOf(
    // Number row
    listOf(
        KeyDef("`", keyCode = KeyCode.GRAVE, shiftLabel = "~"),
        KeyDef("1", keyCode = KeyCode.NUM_1, shiftLabel = "!"),
        KeyDef("2", keyCode = KeyCode.NUM_2, shiftLabel = "@"),
        KeyDef("3", keyCode = KeyCode.NUM_3, shiftLabel = "#"),
        KeyDef("4", keyCode = KeyCode.NUM_4, shiftLabel = "$"),
        KeyDef("5", keyCode = KeyCode.NUM_5, shiftLabel = "%"),
        KeyDef("6", keyCode = KeyCode.NUM_6, shiftLabel = "^"),
        KeyDef("7", keyCode = KeyCode.NUM_7, shiftLabel = "&"),
        KeyDef("8", keyCode = KeyCode.NUM_8, shiftLabel = "*"),
        KeyDef("9", keyCode = KeyCode.NUM_9, shiftLabel = "("),
        KeyDef("0", keyCode = KeyCode.NUM_0, shiftLabel = ")"),
        KeyDef("-", keyCode = KeyCode.MINUS, shiftLabel = "_"),
        KeyDef("=", keyCode = KeyCode.EQUALS, shiftLabel = "+"),
        KeyDef("⌫", isSpecial = true, widthWeight = 1.5f)
    ),
    // QWERTY row
    listOf(
        KeyDef("TAB", isSpecial = true, widthWeight = 1.5f),
        KeyDef("q", keyCode = KeyCode.Q, shiftLabel = "Q"),
        KeyDef("w", keyCode = KeyCode.W, shiftLabel = "W"),
        KeyDef("e", keyCode = KeyCode.E, shiftLabel = "E"),
        KeyDef("r", keyCode = KeyCode.R, shiftLabel = "R"),
        KeyDef("t", keyCode = KeyCode.T, shiftLabel = "T"),
        KeyDef("y", keyCode = KeyCode.Y, shiftLabel = "Y"),
        KeyDef("u", keyCode = KeyCode.U, shiftLabel = "U"),
        KeyDef("i", keyCode = KeyCode.I, shiftLabel = "I"),
        KeyDef("o", keyCode = KeyCode.O, shiftLabel = "O"),
        KeyDef("p", keyCode = KeyCode.P, shiftLabel = "P"),
        KeyDef("[", keyCode = KeyCode.LEFT_BRACKET, shiftLabel = "{"),
        KeyDef("]", keyCode = KeyCode.RIGHT_BRACKET, shiftLabel = "}"),
        KeyDef("\\", keyCode = KeyCode.BACKSLASH, shiftLabel = "|")
    ),
    // ASDF row
    listOf(
        KeyDef("CAPS", isSpecial = true, widthWeight = 1.8f),
        KeyDef("a", keyCode = KeyCode.A, shiftLabel = "A"),
        KeyDef("s", keyCode = KeyCode.S, shiftLabel = "S"),
        KeyDef("d", keyCode = KeyCode.D, shiftLabel = "D"),
        KeyDef("f", keyCode = KeyCode.F, shiftLabel = "F"),
        KeyDef("g", keyCode = KeyCode.G, shiftLabel = "G"),
        KeyDef("h", keyCode = KeyCode.H, shiftLabel = "H"),
        KeyDef("j", keyCode = KeyCode.J, shiftLabel = "J"),
        KeyDef("k", keyCode = KeyCode.K, shiftLabel = "K"),
        KeyDef("l", keyCode = KeyCode.L, shiftLabel = "L"),
        KeyDef(";", keyCode = KeyCode.SEMICOLON, shiftLabel = ":"),
        KeyDef("'", keyCode = KeyCode.APOSTROPHE, shiftLabel = "\""),
        KeyDef("ENTER", isSpecial = true, widthWeight = 2f)
    ),
    // ZXCV row
    listOf(
        KeyDef("SHIFT", isSpecial = true, widthWeight = 2.5f),
        KeyDef("z", keyCode = KeyCode.Z, shiftLabel = "Z"),
        KeyDef("x", keyCode = KeyCode.X, shiftLabel = "X"),
        KeyDef("c", keyCode = KeyCode.C, shiftLabel = "C"),
        KeyDef("v", keyCode = KeyCode.V, shiftLabel = "V"),
        KeyDef("b", keyCode = KeyCode.B, shiftLabel = "B"),
        KeyDef("n", keyCode = KeyCode.N, shiftLabel = "N"),
        KeyDef("m", keyCode = KeyCode.M, shiftLabel = "M"),
        KeyDef(",", keyCode = KeyCode.COMMA, shiftLabel = "<"),
        KeyDef(".", keyCode = KeyCode.PERIOD, shiftLabel = ">"),
        KeyDef("/", keyCode = KeyCode.SLASH, shiftLabel = "?"),
        KeyDef("SHIFT", isSpecial = true, widthWeight = 2.5f)
    ),
    // Space row
    listOf(
        KeyDef("CTRL", isSpecial = true, widthWeight = 1.5f),
        KeyDef("ALT", isSpecial = true, widthWeight = 1.5f),
        KeyDef("SPACE", isSpecial = false, keyCode = KeyCode.SPACE, widthWeight = 6f),
        KeyDef("ALT", isSpecial = true, widthWeight = 1.5f),
        KeyDef("CTRL", isSpecial = true, widthWeight = 1.5f)
    )
)

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isActive: Boolean = false,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isActive -> Primary
                    pressed -> KeyPressed
                    isSpecial -> KeySpecial
                    else -> KeyBackground
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onPress()
                        tryAwaitRelease()
                        pressed = false
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else if (isSpecial) OnDarkSecondary else Color.White,
            fontSize = if (label.length > 3) 10.sp else if (label.length > 1) 11.sp else 14.sp,
            fontWeight = if (isSpecial) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun ModifierChip(label: String, active: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = active,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary,
            selectedLabelColor = Color.White,
            containerColor = DarkSurfaceVariant,
            labelColor = OnDarkSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
