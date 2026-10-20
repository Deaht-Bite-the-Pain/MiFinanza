package com.udb.mifinanza.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Accent500,
    onPrimary = White,
    primaryContainer = Accent100,
    onPrimaryContainer = Accent700,
    background = Ink50,
    onBackground = Ink950,
    surface = White,
    onSurface = Ink950,
    onSurfaceVariant = Ink600,
    outline = Ink300,
    outlineVariant = Ink200,
    error = Danger500,
    errorContainer = Danger100
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun MiFinanzaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
