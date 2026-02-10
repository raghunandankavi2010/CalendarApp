package com.example.googlecalendarclone.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = GreenEvent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F4EA),
    onSecondaryContainer = Color(0xFF1E8E3E),
    tertiary = YellowEvent,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFEF7E0),
    onTertiaryContainer = Color(0xFFB06000),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = OutlineVariant,
    outlineVariant = Color(0xFFE8EAED),
    error = RedEvent,
    onError = Color.White,
    errorContainer = Color(0xFFFCE8E6),
    onErrorContainer = Color(0xFFC5221F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF1B4A8F),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFF81C995),
    onSecondary = Color(0xFF0F5223),
    secondaryContainer = Color(0xFF1E5F2F),
    onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = Color(0xFFFDD663),
    onTertiary = Color(0xFF5F4700),
    tertiaryContainer = Color(0xFF856900),
    onTertiaryContainer = Color(0xFFFFF8E1),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF3C4043),
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF3C4043),
    error = Color(0xFFF28B82),
    onError = Color(0xFF5C1D19),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9D2D0)
)

@Composable
fun GoogleCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
