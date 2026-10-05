package com.example.service_manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Navy,
    onPrimary = White,
    primaryContainer = LightGray,
    onPrimaryContainer = Navy,

    secondary = Orange,
    onSecondary = Navy,
    secondaryContainer = OrangeTint,
    onSecondaryContainer = Navy,

    tertiary = Black,
    onTertiary = White,
    tertiaryContainer = Navy,
    onTertiaryContainer = White,

    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGray,
    onSurfaceVariant = Slate,
    outline = SlateOutline,
    outlineVariant = LightGray,

    // Without these, cards and bars keep Material's default lavender-gray tint
    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF7F7F7),
    surfaceContainerHigh = Color(0xFFF4F4F4),
    surfaceContainerHighest = Color(0xFFF1F1F1),
)

@Composable
fun ServicemanagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = Typography,
        content = content
    )
}