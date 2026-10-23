package com.example.eduvault.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Light Color Scheme (mặc định dùng cho app) ──────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = ColorNavyDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = ColorNavyDark,

    secondary = ColorGreenNeon,
    onSecondary = Color.White,
    secondaryContainer = ColorSuccessBg,
    onSecondaryContainer = ColorSuccessText,

    background = ColorSlateBackground,
    onBackground = ColorTextOnLight,

    surface = ColorSurfaceWhite,
    onSurface = ColorTextOnLight,
    surfaceVariant = ColorBorderLight,
    onSurfaceVariant = ColorTextOnLightSecondary,

    outline = ColorBorder,
    error = ColorError,
    onError = Color.White,
)

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = ColorAmber,
    onPrimary = ColorInk,
    primaryContainer = ColorAmberDark,
    onPrimaryContainer = ColorTextOnDark,

    secondary = ColorForestLight,
    onSecondary = ColorInk,

    background = ColorInk,
    onBackground = ColorTextOnDark,

    surface = ColorInkLight,
    onSurface = ColorTextOnDark,
    surfaceVariant = ColorInkLighter,
    onSurfaceVariant = ColorTextOnDarkSecondary,

    outline = ColorInkLighter,
    error = ColorError,
    onError = ColorTextOnDark,
)

@Composable
fun EduVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
