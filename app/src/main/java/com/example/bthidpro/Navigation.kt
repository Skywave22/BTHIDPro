package com.example.bthidpro

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.bthidpro.ui.HidViewModel
import com.example.bthidpro.ui.screens.DevicePickerScreen
import com.example.bthidpro.ui.screens.GamepadScreen
import com.example.bthidpro.ui.screens.HomeScreen
import com.example.bthidpro.ui.screens.KeyboardScreen
import com.example.bthidpro.ui.screens.MouseScreen
import com.example.bthidpro.ui.screens.SettingsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(HomeRoute)
    val viewModel: HidViewModel = viewModel()
    val context = LocalContext.current

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> backStack.add(route) }
                )
            }
            entry<KeyboardRoute> {
                KeyboardScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<MouseRoute> {
                MouseScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<GamepadRoute> {
                GamepadScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SettingsRoute> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<DevicePickerRoute> {
                DevicePickerScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
