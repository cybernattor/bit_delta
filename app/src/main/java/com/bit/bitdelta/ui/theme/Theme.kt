package com.bit.bitdelta.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DeepBlue,
    surface = NavyBlue,
    onPrimary = DeepBlue,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

object BitDeltaTheme {
    val glassColor: Color = GlassWhite
    val textColor: Color = TextWhite
    val accentColor: Color = SkyBlue
    val backgroundColor: Color = DeepBlue
}

@Composable
fun BitDeltaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Всегда темная тема, поэтому светлые иконки в статус-баре
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
