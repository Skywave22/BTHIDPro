package com.example.bthidpro.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnDarkPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = Accent,
    onSecondary = OnDarkPrimary,
    secondaryContainer = AccentDark,
    onSecondaryContainer = OnDarkPrimary,
    tertiary = Info,
    background = DarkBackground,
    onBackground = OnDarkPrimary,
    surface = DarkSurface,
    onSurface = OnDarkPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    error = Error,
    onError = OnDarkPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = OnDarkPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = AccentDark,
    onSecondary = OnDarkPrimary,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkSurfaceVariant,
    error = Error,
)

@Composable
fun BTHIDProTheme(
    darkTheme: Boolean = true, // Default dark for gaming feel
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
