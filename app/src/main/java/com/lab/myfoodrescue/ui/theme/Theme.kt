package com.lab.myfoodrescue.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FlashGreen80,
    onPrimary = FlashDarkSurface,
    primaryContainer = FlashGreenDark,
    onPrimaryContainer = FlashGreenContainer,
    secondary = FlashSlate80,
    onSecondary = FlashDarkSurface,
    secondaryContainer = FlashSlateDark,
    onSecondaryContainer = FlashSlateContainer,
    tertiary = FlashAmber80,
    onTertiary = Color(0xFF3A2A00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = FlashAmberContainer,
    background = FlashDarkBackground,
    onBackground = Color(0xFFE1E7E2),
    surface = FlashDarkSurface,
    onSurface = Color(0xFFE1E7E2),
    surfaceVariant = Color(0xFF243028),
    onSurfaceVariant = Color(0xFFB9C6BC)
)

private val LightColorScheme = lightColorScheme(
    primary = FlashGreen,
    onPrimary = Color.White,
    primaryContainer = FlashGreenContainer,
    onPrimaryContainer = FlashGreenDark,
    secondary = FlashSlate,
    onSecondary = Color.White,
    secondaryContainer = FlashSlateContainer,
    onSecondaryContainer = FlashSlateDark,
    tertiary = FlashAmber,
    onTertiary = Color(0xFF3A2A00),
    tertiaryContainer = FlashAmberContainer,
    onTertiaryContainer = Color(0xFF3A2A00),
    background = FlashBackground,
    onBackground = Color(0xFF1A201C),
    surface = FlashSurface,
    onSurface = Color(0xFF1A201C),
    surfaceVariant = FlashSurfaceVariant,
    onSurfaceVariant = Color(0xFF414944)
)

@Composable
fun MyFoodRescueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled so the Flash Food Rescue brand palette always shows.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
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