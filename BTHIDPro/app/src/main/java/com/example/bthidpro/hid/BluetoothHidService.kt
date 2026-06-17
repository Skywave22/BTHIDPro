package com.example.bthidpro.hid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bthidpro.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

/**
 * Foreground service that registers as a Bluetooth HID device (keyboard + mouse + gamepad).
 * Uses the Android BluetoothHidDevice API (API 28+).
 */
class BluetoothHidService : Service() {

    companion object {
        private const val TAG = "BluetoothHidService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "bt_hid_channel"

        const val ACTION_START = "com.example.bthidpro.START_HID"
        const val ACTION_STOP = "com.example.bthidpro.STOP_HID"

        // Report IDs matching the descriptor
        const val REPORT_ID_KEYBOARD: Byte = 1
        const val REPORT_ID_MOUSE: Byte = 2
        const val REPORT_ID_GAMEPAD: Byte = 3
    }

    inner class HidBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }

    private val binder = HidBinder()

    private var bluetoothHidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val executor = Executors.newSingleThreadExecutor()

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, device=$pluggedDevice")
            if (registered) {
                _connectionState.value = ConnectionState.REGISTERED
                refreshPairedDevices()
            } else {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: state=$state, device=${device?.name}")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    _connectionState.value = ConnectionState.CONNECTED
                    updateNotification("Connected to ${device?.name ?: "device"}")
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = ConnectionState.CONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    _connectionState.value = ConnectionState.REGISTERED
                    updateNotification("Waiting for connection...")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    _connectionState.value = ConnectionState.DISCONNECTING
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport: type=$type, id=$id")
            // Reply with empty reports
            when (id) {
                REPORT_ID_KEYBOARD -> bluetoothHidDevice?.replyReport(device, type, id, ByteArray(8))
                REPORT_ID_MOUSE -> bluetoothHidDevice?.replyReport(device, type, id, ByteArray(4))
                REPORT_ID_GAMEPAD -> bluetoothHidDevice?.replyReport(device, type, id, ByteArray(8))
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            Log.d(TAG, "onSetReport: type=$type, id=$id")
            bluetoothHidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
        }

        override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
            Log.d(TAG, "onInterruptData: reportId=$reportId")
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                bluetoothHidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                bluetoothHidDevice = null
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Starting Bluetooth HID..."))
        initBluetooth()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        unregisterApp()
        executor.shutdown()
    }

    private fun initBluetooth() {
        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val btAdapter = btManager.adapter
        if (btAdapter == null || !btAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth not available or not enabled")
            _connectionState.value = ConnectionState.ERROR
            return
        }
        btAdapter.getProfileProxy(this, profileListener, BluetoothProfile.HID_DEVICE)
    }

    private fun registerApp() {
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "BT HID Pro",
            "Bluetooth Keyboard, Mouse & Gamepad",
            "Appground",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            HidDescriptors.COMBINED_DESCRIPTOR
        )
        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX
        )
        try {
            bluetoothHidDevice?.registerApp(sdp, null, qos, executor, hidCallback)
            Log.d(TAG, "HID App registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register HID app", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    private fun unregisterApp() {
        try {
            bluetoothHidDevice?.unregisterApp()
            val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            btManager.adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering", e)
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        try {
            bluetoothHidDevice?.connect(device)
            _connectionState.value = ConnectionState.CONNECTING
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect", e)
        }
    }

    fun disconnectDevice() {
        connectedDevice?.let {
            try {
                bluetoothHidDevice?.disconnect(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disconnect", e)
            }
        }
    }

    fun refreshPairedDevices() {
        try {
            val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val paired = btManager.adapter?.bondedDevices?.toList() ?: emptyList()
            _pairedDevices.value = paired
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get paired devices", e)
        }
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────

    fun sendKeyPress(modifier: Byte, keyCode: Byte) {
        val report = ByteArray(8).apply {
            this[0] = modifier
            this[1] = 0x00
            this[2] = keyCode
        }
        sendReport(REPORT_ID_KEYBOARD, report)
    }

    fun sendKeyRelease() {
        sendReport(REPORT_ID_KEYBOARD, ByteArray(8))
    }

    fun sendText(text: String) {
        for (char in text) {
            val (modifier, keyCode) = charToHidKey(char)
            sendKeyPress(modifier, keyCode)
            Thread.sleep(20)
            sendKeyRelease()
            Thread.sleep(10)
        }
    }

    private fun charToHidKey(char: Char): Pair<Byte, Byte> {
        return when (char.lowercaseChar()) {
            'a' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.A)
            'b' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.B)
            'c' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.C)
            'd' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.D)
            'e' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.E)
            'f' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.F)
            'g' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.G)
            'h' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.H)
            'i' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.I)
            'j' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.J)
            'k' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.K)
            'l' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.L)
            'm' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.M)
            'n' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.N)
            'o' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.O)
            'p' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.P)
            'q' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.Q)
            'r' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.R)
            's' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.S)
            't' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.T)
            'u' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.U)
            'v' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.V)
            'w' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.W)
            'x' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.X)
            'y' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.Y)
            'z' -> Pair(if (char.isUpperCase()) KeyCode.MOD_LEFT_SHIFT else KeyCode.MOD_NONE, KeyCode.Z)
            '1' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_1)
            '2' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_2)
            '3' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_3)
            '4' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_4)
            '5' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_5)
            '6' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_6)
            '7' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_7)
            '8' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_8)
            '9' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_9)
            '0' -> Pair(KeyCode.MOD_NONE, KeyCode.NUM_0)
            ' ' -> Pair(KeyCode.MOD_NONE, KeyCode.SPACE)
            '\n' -> Pair(KeyCode.MOD_NONE, KeyCode.ENTER)
            else -> Pair(KeyCode.MOD_NONE, KeyCode.NONE)
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    fun sendMouseMove(dx: Int, dy: Int) {
        val report = ByteArray(4).apply {
            this[0] = 0x00 // No buttons
            this[1] = dx.coerceIn(-127, 127).toByte()
            this[2] = dy.coerceIn(-127, 127).toByte()
            this[3] = 0x00 // No scroll
        }
        sendReport(REPORT_ID_MOUSE, report)
    }

    fun sendMouseButton(leftDown: Boolean, rightDown: Boolean, middleDown: Boolean) {
        val buttons = (if (leftDown) 0x01 else 0) or
                (if (rightDown) 0x02 else 0) or
                (if (middleDown) 0x04 else 0)
        val report = ByteArray(4).apply {
            this[0] = buttons.toByte()
            this[1] = 0
            this[2] = 0
            this[3] = 0
        }
        sendReport(REPORT_ID_MOUSE, report)
    }

    fun sendMouseScroll(scroll: Int) {
        val report = ByteArray(4).apply {
            this[0] = 0x00
            this[1] = 0x00
            this[2] = 0x00
            this[3] = scroll.coerceIn(-127, 127).toByte()
        }
        sendReport(REPORT_ID_MOUSE, report)
    }

    // ── Gamepad ───────────────────────────────────────────────────────────────

    fun sendGamepadState(
        buttons: Int,
        leftX: Int, leftY: Int,
        rightX: Int, rightY: Int,
        leftTrigger: Int, rightTrigger: Int
    ) {
        val report = ByteArray(8).apply {
            this[0] = (buttons and 0xFF).toByte()
            this[1] = ((buttons shr 8) and 0xFF).toByte()
            this[2] = leftX.coerceIn(-127, 127).toByte()
            this[3] = leftY.coerceIn(-127, 127).toByte()
            this[4] = rightX.coerceIn(-127, 127).toByte()
            this[5] = rightY.coerceIn(-127, 127).toByte()
            this[6] = leftTrigger.coerceIn(-127, 127).toByte()
            this[7] = rightTrigger.coerceIn(-127, 127).toByte()
        }
        sendReport(REPORT_ID_GAMEPAD, report)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun sendReport(reportId: Byte, report: ByteArray) {
        val device = connectedDevice ?: return
        try {
            bluetoothHidDevice?.sendReport(device, reportId.toInt(), report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send report $reportId", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "BT HID Pro Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps Bluetooth HID active"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = Intent(this, BluetoothHidService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BT HID Pro")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pi)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPi)
            .build()
    }

    private fun updateNotification(message: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(message))
    }
}

enum class ConnectionState {
    DISCONNECTED, REGISTERED, CONNECTING, CONNECTED, DISCONNECTING, ERROR
}
