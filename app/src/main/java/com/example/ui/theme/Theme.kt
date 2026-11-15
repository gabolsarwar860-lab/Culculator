package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = DarkEqualsKey,
    onPrimary = DarkEqualsText,
    primaryContainer = DarkOperatorKey,
    onPrimaryContainer = DarkOperatorText,
    secondary = DarkFunctionKey,
    onSecondary = DarkInkPrimary,
    background = DarkBackground,
    onBackground = DarkInkPrimary,
    surface = DarkDisplaySurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkDigitKey,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkDisplayBorder,
    error = DarkClearText,
    errorContainer = DarkClearKey,
    onErrorContainer = DarkClearText
)

private val LightColorScheme = lightColorScheme(
    primary = LightEqualsKey,
    onPrimary = LightEqualsText,
    primaryContainer = LightOperatorKey,
    onPrimaryContainer = LightOperatorText,
    secondary = LightFunctionKey,
    onSecondary = LightInkPrimary,
    background = LightBackground,
    onBackground = LightInkPrimary,
    surface = LightDisplaySurface,
    onSurface = LightInkPrimary,
    surfaceVariant = LightScientificKey,
    onSurfaceVariant = LightInkSecondary,
    outline = LightDisplayBorder,
    error = LightClearText,
    errorContainer = LightClearKey,
    onErrorContainer = LightClearText
)

val LocalCalculatorColors = staticCompositionLocalOf { DarkCalculatorColors }

object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCalculatorColors.current
}

@Composable
fun CalculatorAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val calcColors = if (darkTheme) DarkCalculatorColors else LightCalculatorColors

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
