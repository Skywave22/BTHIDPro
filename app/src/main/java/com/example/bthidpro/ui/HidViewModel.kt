package com.example.bthidpro.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bthidpro.hid.BluetoothHidService
import com.example.bthidpro.hid.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HidViewModel(application: Application) : AndroidViewModel(application) {

    private var hidService: BluetoothHidService? = null
    private var bound = false

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _sensitivity = MutableStateFlow(5f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _scrollSpeed = MutableStateFlow(3f)
    val scrollSpeed: StateFlow<Float> = _scrollSpeed.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _gamepadButtons = MutableStateFlow(0)
    val gamepadButtons: StateFlow<Int> = _gamepadButtons.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothHidService.HidBinder
            hidService = binder.getService()
            bound = true
            // Continuously collect state from the service
            viewModelScope.launch {
                hidService?.connectionState?.collect { state ->
                    _connectionState.value = state
                }
            }
            viewModelScope.launch {
                hidService?.pairedDevices?.collect { devices ->
                    _pairedDevices.value = devices
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            hidService = null
            bound = false
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    fun startAndBindService(context: Context) {
        val intent = Intent(context, BluetoothHidService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        hidService?.connectToDevice(device)
    }

    fun disconnect() {
        hidService?.disconnectDevice()
    }

    fun refreshDevices() {
        hidService?.refreshPairedDevices()
        hidService?.pairedDevices?.let { _pairedDevices.value = it.value }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSensitivity(value: Float) {
        _sensitivity.value = value
    }

    fun setScrollSpeed(value: Float) {
        _scrollSpeed.value = value
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────

    fun sendKeyPress(modifier: Byte, keyCode: Byte) {
        hidService?.sendKeyPress(modifier, keyCode)
    }

    fun sendKeyRelease() {
        hidService?.sendKeyRelease()
    }

    fun sendText(text: String) {
        hidService?.sendText(text)
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    fun sendMouseMove(dx: Int, dy: Int) {
        val scale = _sensitivity.value
        hidService?.sendMouseMove((dx * scale).toInt(), (dy * scale).toInt())
    }

    fun sendMouseButton(left: Boolean = false, right: Boolean = false, middle: Boolean = false) {
        hidService?.sendMouseButton(left, right, middle)
    }

    fun sendMouseScroll(scroll: Int) {
        val scale = _scrollSpeed.value
        hidService?.sendMouseScroll((scroll * scale).toInt())
    }

    // ── Gamepad ───────────────────────────────────────────────────────────────

    fun pressGamepadButton(button: Int) {
        _gamepadButtons.value = _gamepadButtons.value or button
        sendGamepadState()
    }

    fun releaseGamepadButton(button: Int) {
        _gamepadButtons.value = _gamepadButtons.value and button.inv()
        sendGamepadState()
    }

    private var leftX = 0
    private var leftY = 0
    private var rightX = 0
    private var rightY = 0
    private var leftTrigger = 0
    private var rightTrigger = 0

    fun updateLeftStick(x: Int, y: Int) {
        leftX = x; leftY = y
        sendGamepadState()
    }

    fun updateRightStick(x: Int, y: Int) {
        rightX = x; rightY = y
        sendGamepadState()
    }

    fun updateTriggers(left: Int, right: Int) {
        leftTrigger = left; rightTrigger = right
        sendGamepadState()
    }

    private fun sendGamepadState() {
        hidService?.sendGamepadState(
            _gamepadButtons.value,
            leftX, leftY, rightX, rightY,
            leftTrigger, rightTrigger
        )
    }

    override fun onCleared() {
        super.onCleared()
        leftX = 0; leftY = 0; rightX = 0; rightY = 0
    }
}
