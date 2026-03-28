package com.squadsports.sdk.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Parse a hex color string to Compose Color.
 */
fun parseColor(hex: String): Color {
    val sanitized = hex.removePrefix("#")
    return try {
        Color(android.graphics.Color.parseColor("#$sanitized"))
    } catch (e: Exception) {
        Color(0xFF6E82E7) // Default purple
    }
}

private val SquadDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6E82E7),
    secondary = Color(0xFF566BD7),
    tertiary = Color(0xFF6E82E7),
    background = Color(0xFF111111),
    surface = Color(0xFF212121),
    onPrimary = Color(0xFF0A0A0A),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

/**
 * Squad theme wrapper for Jetpack Compose.
 * Ported from squad-demo/src/theme/theme.tsx.
 */
@Composable
fun SquadTheme(
    primaryColor: Color = Color(0xFF6E82E7),
    content: @Composable () -> Unit,
) {
    val colorScheme = SquadDarkColorScheme.copy(
        primary = primaryColor,
        onPrimary = Color(0xFF0A0A0A),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
