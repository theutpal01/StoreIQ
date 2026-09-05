package com.storiq.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    primaryContainer = Color(0xFF5E35B1),
    secondary = Color(0xFF64B5F6),
    secondaryContainer = Color(0xFF1E88E5),
    tertiary = Color(0xFF81C784),
    tertiaryContainer = Color(0xFF388E3C),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    background = Color(0xFF0A0A0A),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    onSecondary = Color.Black,
    onSecondaryContainer = Color.White,
    onTertiary = Color.Black,
    onTertiaryContainer = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    onBackground = Color.White,
    onError = Color.Black,
    outline = Color(0xFF707070),
    outlineVariant = Color(0xFF404040),
    inverseSurface = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = Color(0xFF5E35B1),
    shadow = Color.Black,
    scrim = Color(0xCC000000),
    surfaceTint = Color(0xFF7C4DFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5E35B1),
    primaryContainer = Color(0xFFEDE7F6),
    secondary = Color(0xFF1E88E5),
    secondaryContainer = Color(0xFFBBDEFB),
    tertiary = Color(0xFF388E3C),
    tertiaryContainer = Color(0xFFC8E6C9),
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFF5F5F5),
    background = Color(0xFFFFFFFF),
    error = Color(0xFFC62828),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF1A003F),
    onSecondary = Color.White,
    onSecondaryContainer = Color(0xFF001F3D),
    onTertiary = Color.White,
    onTertiaryContainer = Color(0xFF002000),
    onSurface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF424242),
    onBackground = Color(0xFF121212),
    onError = Color.White,
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD),
    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFFB39DDB),
    shadow = Color.Black,
    scrim = Color(0xCC000000),
    surfaceTint = Color(0xFF5E35B1)
)

@Composable
fun StorIQTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}