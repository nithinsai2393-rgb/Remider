package com.example.remider.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4361EE), // Vibrant Indigo
    secondary = Color(0xFF3F37C9), // Deep Blue
    tertiary = Color(0xFF4CC9F0), // Sky Blue
    background = Color(0xFFF8F9FF), // Very Light Blue-Grey
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2B2D42), // Dark Charcoal
    onSurface = Color(0xFF2B2D42),
    surfaceVariant = Color(0xFFEDF2FB), // Soft Periwinkle
    onSurfaceVariant = Color(0xFF4A4E69)
)

@Composable
fun RemiderTheme(
    darkTheme: Boolean = false, // Always false for forced Light Mode
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
