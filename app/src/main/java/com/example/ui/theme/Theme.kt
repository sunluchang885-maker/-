package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = GreenDarkContainer,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = AmberDarkAccent,
    onSecondary = Color.Black,
    error = RedDarkPrimary,
    onError = Color.Black,
    errorContainer = RedDarkContainer,
    onErrorContainer = Color(0xFFFFCDD2),
    background = DarkBackground,
    onBackground = Color(0xFFE2E2E9),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC5C6D0)
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = AmberAccent,
    onSecondary = Color.Black,
    error = RedPrimary,
    onError = Color.White,
    errorContainer = RedLight,
    onErrorContainer = Color(0xFFB71C1C),
    background = LightBackground,
    onBackground = Color(0xFF1A1C1E),
    surface = LightSurface,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun CalendarCheckInTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScaleFactor: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val currentDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScaleFactor
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
