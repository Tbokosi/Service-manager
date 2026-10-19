package com.example.service_manager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightAppColors = lightColorScheme(
    primary = SaddleBrown,
    onPrimary = White,
    primaryContainer = SoftSlateTint,
    onPrimaryContainer = Espresso,

    secondary = Terracotta,
    onSecondary = White,
    secondaryContainer = TerracottaTint,
    onSecondaryContainer = SaddleBrown,

    tertiary = Espresso,
    onTertiary = White,
    tertiaryContainer = SoftSlate,
    onTertiaryContainer = DarkEarth,

    background = WarmBackground,
    onBackground = DarkEarth,
    surface = White,
    onSurface = DarkEarth,
    surfaceVariant = SoftSlateTint,
    onSurfaceVariant = Espresso,
    outline = SlateGray,
    outlineVariant = SoftSlate,

    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFF7FAFC),
    surfaceContainer = Color(0xFFF0F4F7),
    surfaceContainerHigh = Color(0xFFE8EEF2),
    surfaceContainerHighest = SoftSlate,
)

private val DarkAppColors = darkColorScheme(
    primary = Terracotta,
    onPrimary = DarkEarth,
    primaryContainer = SaddleBrown,
    onPrimaryContainer = TerracottaTint,

    secondary = SoftSlate,
    onSecondary = DarkEarth,
    secondaryContainer = Espresso,
    onSecondaryContainer = SoftSlateTint,

    tertiary = TerracottaTint,
    onTertiary = DarkEarth,
    tertiaryContainer = SaddleBrown,
    onTertiaryContainer = SoftSlateTint,

    background = DarkBackground,
    onBackground = Color(0xFFF0EAE5),
    surface = DarkSurface,
    onSurface = Color(0xFFF0EAE5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SoftSlate,
    outline = SlateGray,
    outlineVariant = Espresso,

    surfaceContainerLowest = Color(0xFF0C0804),
    surfaceContainerLow = DarkBackground,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF3B2E25),
)

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun ServicemanagerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkAppColors else LightAppColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
