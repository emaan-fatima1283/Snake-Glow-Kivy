package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonCyberColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00381E),
    onPrimaryContainer = NeonGreen,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003747),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4A0019),
    onTertiaryContainer = NeonPink,
    background = CyberDark,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberCardBorder,
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonCyberColorScheme,
        typography = Typography,
        content = content
    )
}
