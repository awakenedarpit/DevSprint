package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HackTrackPrimaryDark,
    onPrimary = Color(0xFF19134D),
    primaryContainer = Color(0xFF332B74),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = HackTrackSecondaryDark,
    onSecondary = Color(0xFF221C58),
    secondaryContainer = Color(0xFF3B357A),
    onSecondaryContainer = Color(0xFFF3F0FF),
    tertiary = HackTrackTertiaryDark,
    background = HackTrackBgDark,
    surface = HackTrackSurfaceDark,
    surfaceVariant = HackTrackSurfaceVariantDark,
    outline = HackTrackOutlineDark,
    onBackground = Color(0xFFF1F0FA),
    onSurface = Color(0xFFF1F0FA),
    onSurfaceVariant = Color(0xFFC7C5DD)
)

private val LightColorScheme = lightColorScheme(
    primary = HackTrackPrimary,
    onPrimary = Color.White,
    primaryContainer = HackTrackPrimaryContainer,
    onPrimaryContainer = HackTrackOnPrimaryContainer,
    secondary = HackTrackSecondary,
    onSecondary = Color.White,
    secondaryContainer = HackTrackSecondaryContainer,
    onSecondaryContainer = Color(0xFF302773),
    tertiary = HackTrackTertiary,
    background = HackTrackBgLight,
    surface = HackTrackSurfaceLight,
    surfaceVariant = HackTrackSurfaceVariantLight,
    outline = HackTrackOutlineLight,
    onBackground = Color(0xFF1E2130),
    onSurface = Color(0xFF1E2130),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun HackTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep HackTrack signature branding by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
