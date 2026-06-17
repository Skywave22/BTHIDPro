package com.example.bthidpro.hid

/**
 * HID Report Descriptors for Keyboard, Mouse, and Gamepad.
 * These are standard USB HID Report Descriptors that the Android BluetoothHidDevice API uses.
 */
object HidDescriptors {

    /**
     * Keyboard HID Report Descriptor
     * Report ID: 1 - 8 bytes: modifier, reserved, keycodes[6]
     */
    val KEYBOARD_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),  // Usage Page (Generic Desktop)
        0x09.toByte(), 0x06.toByte(),  // Usage (Keyboard)
        0xA1.toByte(), 0x01.toByte(),  // Collection (Application)
        0x85.toByte(), 0x01.toByte(),  //   Report ID (1)
        // Modifier keys
        0x05.toByte(), 0x07.toByte(),  //   Usage Page (Key Codes)
        0x19.toByte(), 0xE0.toByte(),  //   Usage Minimum (224)
        0x29.toByte(), 0xE7.toByte(),  //   Usage Maximum (231)
        0x15.toByte(), 0x00.toByte(),  //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),  //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),  //   Report Size (1)
        0x95.toByte(), 0x08.toByte(),  //   Report Count (8)
        0x81.toByte(), 0x02.toByte(),  //   Input (Data, Var, Abs)
        // Reserved byte
        0x95.toByte(), 0x01.toByte(),  //   Report Count (1)
        0x75.toByte(), 0x08.toByte(),  //   Report Size (8)
        0x81.toByte(), 0x01.toByte(),  //   Input (Const)
        // Key array (6 keys)
        0x95.toByte(), 0x06.toByte(),  //   Report Count (6)
        0x75.toByte(), 0x08.toByte(),  //   Report Size (8)
        0x15.toByte(), 0x00.toByte(),  //   Logical Minimum (0)
        0x25.toByte(), 0x65.toByte(),  //   Logical Maximum (101)
        0x05.toByte(), 0x07.toByte(),  //   Usage Page (Key Codes)
        0x19.toByte(), 0x00.toByte(),  //   Usage Minimum (0)
        0x29.toByte(), 0x65.toByte(),  //   Usage Maximum (101)
        0x81.toByte(), 0x00.toByte(),  //   Input (Data, Array)
        0xC0.toByte()                  // End Collection
    )

    /**
     * Mouse HID Report Descriptor
     * Report ID: 2 - buttons, X, Y, wheel
     */
    val MOUSE_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),  // Usage Page (Generic Desktop)
        0x09.toByte(), 0x02.toByte(),  // Usage (Mouse)
        0xA1.toByte(), 0x01.toByte(),  // Collection (Application)
        0x85.toByte(), 0x02.toByte(),  //   Report ID (2)
        0x09.toByte(), 0x01.toByte(),  //   Usage (Pointer)
        0xA1.toByte(), 0x00.toByte(),  //   Collection (Physical)
        // Buttons
        0x05.toByte(), 0x09.toByte(),  //     Usage Page (Buttons)
        0x19.toByte(), 0x01.toByte(),  //     Usage Minimum (1)
        0x29.toByte(), 0x05.toByte(),  //     Usage Maximum (5)
        0x15.toByte(), 0x00.toByte(),  //     Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),  //     Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),  //     Report Size (1)
        0x95.toByte(), 0x05.toByte(),  //     Report Count (5)
        0x81.toByte(), 0x02.toByte(),  //     Input (Data, Var, Abs)
        // Padding
        0x75.toByte(), 0x03.toByte(),  //     Report Size (3)
        0x95.toByte(), 0x01.toByte(),  //     Report Count (1)
        0x81.toByte(), 0x01.toByte(),  //     Input (Const)
        // X, Y relative movement
        0x05.toByte(), 0x01.toByte(),  //     Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),  //     Usage (X)
        0x09.toByte(), 0x31.toByte(),  //     Usage (Y)
        0x15.toByte(), 0x81.toByte(),  //     Logical Minimum (-127)
        0x25.toByte(), 0x7F.toByte(),  //     Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(),  //     Report Size (8)
        0x95.toByte(), 0x02.toByte(),  //     Report Count (2)
        0x81.toByte(), 0x06.toByte(),  //     Input (Data, Var, Rel)
        // Wheel
        0x09.toByte(), 0x38.toByte(),  //     Usage (Wheel)
        0x15.toByte(), 0x81.toByte(),  //     Logical Minimum (-127)
        0x25.toByte(), 0x7F.toByte(),  //     Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(),  //     Report Size (8)
        0x95.toByte(), 0x01.toByte(),  //     Report Count (1)
        0x81.toByte(), 0x06.toByte(),  //     Input (Data, Var, Rel)
        0xC0.toByte(),                 //   End Collection
        0xC0.toByte()                  // End Collection
    )

    /**
     * Gamepad HID Report Descriptor
     * Report ID: 3 - buttons, left stick X/Y, right stick X/Y, triggers
     */
    val GAMEPAD_DESCRIPTOR: ByteArray = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),  // Usage Page (Generic Desktop)
        0x09.toByte(), 0x05.toByte(),  // Usage (Game Pad)
        0xA1.toByte(), 0x01.toByte(),  // Collection (Application)
        0x85.toByte(), 0x03.toByte(),  //   Report ID (3)
        // 16 Buttons
        0x05.toByte(), 0x09.toByte(),  //   Usage Page (Button)
        0x19.toByte(), 0x01.toByte(),  //   Usage Minimum (Button 1)
        0x29.toByte(), 0x10.toByte(),  //   Usage Maximum (Button 16)
        0x15.toByte(), 0x00.toByte(),  //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),  //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),  //   Report Size (1)
        0x95.toByte(), 0x10.toByte(),  //   Report Count (16)
        0x81.toByte(), 0x02.toByte(),  //   Input (Data, Var, Abs)
        // Left stick X, Y
        0x05.toByte(), 0x01.toByte(),  //   Usage Page (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),  //   Usage (X)
        0x09.toByte(), 0x31.toByte(),  //   Usage (Y)
        // Right stick X, Y
        0x09.toByte(), 0x32.toByte(),  //   Usage (Z)
        0x09.toByte(), 0x35.toByte(),  //   Usage (Rz)
        // Triggers
        0x09.toByte(), 0x33.toByte(),  //   Usage (Rx) - Left trigger
        0x09.toByte(), 0x34.toByte(),  //   Usage (Ry) - Right trigger
        0x15.toByte(), 0x81.toByte(),  //   Logical Minimum (-127)
        0x25.toByte(), 0x7F.toByte(),  //   Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(),  //   Report Size (8)
        0x95.toByte(), 0x06.toByte(),  //   Report Count (6)
        0x81.toByte(), 0x02.toByte(),  //   Input (Data, Var, Abs)
        0xC0.toByte()                  // End Collection
    )

    /** Combined descriptor for all three devices */
    val COMBINED_DESCRIPTOR: ByteArray =
        KEYBOARD_DESCRIPTOR + MOUSE_DESCRIPTOR + GAMEPAD_DESCRIPTOR
}

/** HID key codes for standard keyboard keys */
object KeyCode {
    const val NONE: Byte = 0x00
    const val A: Byte = 0x04
    const val B: Byte = 0x05
    const val C: Byte = 0x06
    const val D: Byte = 0x07
    const val E: Byte = 0x08
    const val F: Byte = 0x09
    const val G: Byte = 0x0A
    const val H: Byte = 0x0B
    const val I: Byte = 0x0C
    const val J: Byte = 0x0D
    const val K: Byte = 0x0E
    const val L: Byte = 0x0F
    const val M: Byte = 0x10
    const val N: Byte = 0x11
    const val O: Byte = 0x12
    const val P: Byte = 0x13
    const val Q: Byte = 0x14
    const val R: Byte = 0x15
    const val S: Byte = 0x16
    const val T: Byte = 0x17
    const val U: Byte = 0x18
    const val V: Byte = 0x19
    const val W: Byte = 0x1A
    const val X: Byte = 0x1B
    const val Y: Byte = 0x1C
    const val Z: Byte = 0x1D
    const val NUM_1: Byte = 0x1E
    const val NUM_2: Byte = 0x1F
    const val NUM_3: Byte = 0x20
    const val NUM_4: Byte = 0x21
    const val NUM_5: Byte = 0x22
    const val NUM_6: Byte = 0x23
    const val NUM_7: Byte = 0x24
    const val NUM_8: Byte = 0x25
    const val NUM_9: Byte = 0x26
    const val NUM_0: Byte = 0x27
    const val ENTER: Byte = 0x28
    const val ESCAPE: Byte = 0x29
    const val BACKSPACE: Byte = 0x2A
    const val TAB: Byte = 0x2B
    const val SPACE: Byte = 0x2C
    const val MINUS: Byte = 0x2D
    const val EQUALS: Byte = 0x2E
    const val LEFT_BRACKET: Byte = 0x2F
    const val RIGHT_BRACKET: Byte = 0x30
    const val BACKSLASH: Byte = 0x31
    const val SEMICOLON: Byte = 0x33
    const val APOSTROPHE: Byte = 0x34
    const val GRAVE: Byte = 0x35
    const val COMMA: Byte = 0x36
    const val PERIOD: Byte = 0x37
    const val SLASH: Byte = 0x38
    const val CAPS_LOCK: Byte = 0x39
    const val F1: Byte = 0x3A
    const val F2: Byte = 0x3B
    const val F3: Byte = 0x3C
    const val F4: Byte = 0x3D
    const val F5: Byte = 0x3E
    const val F6: Byte = 0x3F
    const val F7: Byte = 0x40
    const val F8: Byte = 0x41
    const val F9: Byte = 0x42
    const val F10: Byte = 0x43
    const val F11: Byte = 0x44
    const val F12: Byte = 0x45
    const val DELETE: Byte = 0x4C
    const val RIGHT_ARROW: Byte = 0x4F
    const val LEFT_ARROW: Byte = 0x50
    const val DOWN_ARROW: Byte = 0x51
    const val UP_ARROW: Byte = 0x52
    const val LEFT_CTRL: Byte = 0x00  // Modifier
    const val LEFT_SHIFT: Byte = 0x02 // Modifier bit 1
    const val LEFT_ALT: Byte = 0x04   // Modifier bit 2

    // Modifier flags
    const val MOD_NONE: Byte = 0x00
    const val MOD_LEFT_CTRL: Byte = 0x01
    const val MOD_LEFT_SHIFT: Byte = 0x02
    const val MOD_LEFT_ALT: Byte = 0x04
    const val MOD_LEFT_GUI: Byte = 0x08
    const val MOD_RIGHT_CTRL: Byte = 0x10
    const val MOD_RIGHT_SHIFT: Byte = 0x20
    const val MOD_RIGHT_ALT: Byte = 0x40
    const val MOD_RIGHT_GUI: Byte = (-128).toByte() // 0x80
}

/** Gamepad button bit flags */
object GamepadButton {
    const val CROSS = 0x0001      // A / Cross
    const val CIRCLE = 0x0002     // B / Circle
    const val SQUARE = 0x0004     // X / Square
    const val TRIANGLE = 0x0008   // Y / Triangle
    const val L1 = 0x0010
    const val R1 = 0x0020
    const val L2 = 0x0040
    const val R2 = 0x0080
    const val SELECT = 0x0100
    const val START = 0x0200
    const val L3 = 0x0400
    const val MENU = 0x0400   // alias for L3 (used by GamepadScreen)
    const val R3 = 0x0800
    const val DPAD_UP = 0x1000
    const val DPAD_DOWN = 0x2000
    const val DPAD_LEFT = 0x4000
    const val DPAD_RIGHT = 0x8000
}
